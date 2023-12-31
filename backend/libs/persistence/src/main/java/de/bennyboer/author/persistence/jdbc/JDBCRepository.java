package de.bennyboer.author.persistence.jdbc;

import de.bennyboer.author.persistence.Repository;
import de.bennyboer.author.persistence.RepositoryVersion;
import de.bennyboer.author.persistence.sqlite.SQLiteRepository;
import jakarta.annotation.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class JDBCRepository implements Repository {

    private static final String META_DATA_TABLE_NAME = "meta_data";

    @Nullable
    private Connection connection;

    protected abstract Connection createConnection() throws SQLException, IOException;

    protected abstract boolean tableExists(Connection connection, String tableName) throws SQLException;

    protected abstract void initializeMetaDataTable(Connection connection, String tableName) throws SQLException;

    protected abstract void patchIfNeeded(Connection connection) throws SQLException;

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
            patchIfNeeded(connection);
        }
    }

    protected Connection getConnection() throws SQLException, IOException {
        if (connection == null || connection.isClosed()) {
            connection = createConnection();
        }

        return connection;
    }

    protected <T> Mono<T> executeSqlQueryWithOneResult(
            String sql,
            SQLiteRepository.PreparedStatementConfig statementConfig,
            SQLiteRepository.ResultSetRowMapper<T> resultRowMapper
    ) {
        return executeSqlQuery(sql, statementConfig, resultRowMapper).next();
    }

    protected <T> Flux<T> executeSqlQuery(
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

    public interface PreparedStatementConfig {

        void accept(PreparedStatement statement) throws SQLException;

    }

    public interface ResultSetRowMapper<T> {

        T map(ResultSet resultSet) throws SQLException;

    }

}
