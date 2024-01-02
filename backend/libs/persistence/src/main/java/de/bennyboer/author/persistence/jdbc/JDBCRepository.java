package de.bennyboer.author.persistence.jdbc;

import de.bennyboer.author.persistence.Repository;
import de.bennyboer.author.persistence.RepositoryVersion;
import de.bennyboer.author.persistence.patches.JDBCRepositoryPatch;
import de.bennyboer.author.persistence.patches.PatchManager;
import de.bennyboer.author.persistence.sqlite.SQLiteRepository;
import jakarta.annotation.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

public abstract class JDBCRepository implements Repository {

    private static final String META_DATA_TABLE_NAME = "meta_data";

    @Nullable
    private Connection connection;

    private final PatchManager<JDBCRepositoryPatch> patchManager;

    protected JDBCRepository(PatchManager<JDBCRepositoryPatch> patchManager) {
        this.patchManager = patchManager;
    }

    protected abstract Connection createConnection() throws SQLException, IOException;

    protected abstract boolean tableExists(Connection connection, String tableName) throws SQLException;

    protected abstract void initializeMetaDataTable(Connection connection, String tableName) throws SQLException;

    protected abstract RepositoryVersion readVersionFromMetaDataTable(
            Connection connection,
            String metaDataTableName
    ) throws SQLException;

    protected abstract void initialize(Connection connection) throws SQLException;

    public Mono<Connection> getConnectionMono() {
        return Mono.fromCallable(this::getConnection);
    }

    @Override
    public Mono<RepositoryVersion> getVersion() {
        return Mono.fromCallable(() -> readVersionFromMetaDataTable(getConnection(), META_DATA_TABLE_NAME));
    }

    @Override
    public void close() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    protected void initializeOrPatch() throws SQLException, IOException {
        Connection connection = getConnection();

        if (isMetaDataTableMissing(connection)) {
            initializeMetaDataTable(connection, META_DATA_TABLE_NAME);
            initialize(connection);
        } else {
            patchIfNecessary(connection);
        }
    }

    protected Connection getConnection() throws SQLException, IOException {
        if (connection == null || connection.isClosed()) {
            connection = createConnection();
        }

        return connection;
    }

    protected <T> Mono<T> queryOne(
            String sql,
            SQLiteRepository.PreparedStatementConfig statementConfig,
            SQLiteRepository.ResultSetRowMapper<T> resultRowMapper
    ) {
        return query(sql, statementConfig, resultRowMapper).next();
    }

    protected <T> Flux<T> query(
            String sql,
            SQLiteRepository.PreparedStatementConfig statementConfig,
            SQLiteRepository.ResultSetRowMapper<T> resultRowMapper
    ) {
        return getConnectionMono()
                .flatMapMany(connection -> Flux.usingWhen(
                        Mono.fromCallable(() -> connection.prepareStatement(sql)),
                        statement -> {
                            try {
                                statementConfig.accept(statement);
                            } catch (SQLException e) {
                                return Mono.error(e);
                            }

                            return Mono.fromCallable(statement::executeQuery)
                                    .flatMapMany(resultSet -> toFlux(resultSet, resultRowMapper));
                        },
                        statement -> Mono.fromCallable(() -> {
                            try {
                                statement.close();
                                return Mono.empty();
                            } catch (SQLException e) {
                                return Mono.error(e);
                            }
                        })
                ));
    }

    protected Mono<Integer> update(String sql, PreparedStatementConfig statementConfig) {
        return getConnectionMono()
                .flatMap(connection -> {
                    try (var statement = connection.prepareStatement(sql)) {
                        statementConfig.accept(statement);
                        return Mono.just(statement.executeUpdate());
                    } catch (SQLException e) {
                        return Mono.error(e);
                    }
                });
    }

    protected <T> Mono<Void> batchUpdate(
            String sql,
            Collection<T> objects,
            PreparedStatementObjectConfig<T> statementConfig
    ) {
        return batchUpdate(sql, objects, statementConfig, 1000);
    }

    protected <T> Mono<Void> batchUpdate(
            String sql,
            Collection<T> objects,
            PreparedStatementObjectConfig<T> statementConfig,
            int batchSize
    ) {
        return getConnectionMono()
                .flatMap(connection -> {
                    try (var statement = connection.prepareStatement(sql)) {
                        int counter = 0;

                        for (T object : objects) {
                            statementConfig.accept(statement, object);
                            statement.addBatch();

                            counter += 1;

                            if (counter % batchSize == 0 || counter == objects.size()) {
                                statement.executeBatch();
                            }
                        }

                        return Mono.empty();
                    } catch (SQLException e) {
                        return Mono.error(e);
                    }
                });
    }

    private <T> Flux<T> toFlux(ResultSet resultSet, SQLiteRepository.ResultSetRowMapper<T> rowMapper) {
        return Flux.create(sink -> {
            try {
                while (resultSet.next()) {
                    sink.next(rowMapper.map(resultSet));
                }

                sink.complete();
            } catch (SQLException e) {
                sink.error(e);
            }
        });
    }

    private boolean isMetaDataTableMissing(Connection connection) throws SQLException {
        return !tableExists(connection, META_DATA_TABLE_NAME);
    }

    private void patchIfNecessary(Connection connection) throws SQLException {
        List<JDBCRepositoryPatch> patches = patchManager.findUnappliedPatches(this).block();

        for (JDBCRepositoryPatch patch : patches) {
            patchInTransaction(connection, patch);
        }
    }

    private void patchInTransaction(Connection connection, JDBCRepositoryPatch patch) throws SQLException {
        boolean wasAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);

        try {
            patch.apply(connection);
            updateVersion(connection, patch.appliesTo().increase());

            connection.commit();
        } catch (Exception e) {
            connection.rollback();
        } finally {
            connection.commit();
            connection.setAutoCommit(wasAutoCommit);
        }
    }

    private void updateVersion(Connection connection, RepositoryVersion version) throws SQLException {
        String sql = """
                UPDATE %s SET value = ? WHERE key = 'version'
                """.formatted(META_DATA_TABLE_NAME);

        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, version.getValue());
            statement.executeUpdate();
        }
    }

    public interface PreparedStatementConfig {

        void accept(PreparedStatement statement) throws SQLException;

    }

    public interface PreparedStatementObjectConfig<T> {

        void accept(PreparedStatement statement, T object) throws SQLException;

    }

    public interface ResultSetRowMapper<T> {

        T map(ResultSet resultSet) throws SQLException;

    }

}
