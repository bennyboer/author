package de.bennyboer.author.persistence.sqlite;

import de.bennyboer.author.persistence.RepositoryVersion;
import de.bennyboer.author.persistence.jdbc.JDBCRepository;
import org.apache.commons.lang3.SystemUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;

public class SQLiteRepository extends JDBCRepository {

    private static final String FILE_EXTENSION = "sqlite3";

    private final String name;
    private final boolean isTemporary;

    public SQLiteRepository(String name, boolean isTemporary) {
        super();

        this.name = name;
        this.isTemporary = isTemporary;

        try {
            initializeOrPatch();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public SQLiteRepository(String name) {
        this(name, false);
    }

    @Override
    protected void initialize(Connection connection) throws SQLException {
    }

    @Override
    protected Connection createConnection() throws SQLException {
        String url = String.format("jdbc:sqlite:%s", getFilePath().toAbsolutePath());

        return DriverManager.getConnection(url);
    }

    @Override
    protected boolean tableExists(Connection connection, String tableName) throws SQLException {
        try (var statement = connection.createStatement()) {
            statement.execute(String.format(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name='%s'",
                    tableName
            ));

            return statement.getResultSet().next();
        }
    }

    @Override
    protected void initializeMetaDataTable(Connection connection, String tableName) throws SQLException {
        try (var statement = connection.createStatement()) {
            statement.execute(String.format(
                    """
                            CREATE TABLE %s (
                                key text PRIMARY KEY,
                                value text NOT NULL
                            )
                            """,
                    tableName
            ));

            statement.execute(String.format(
                    """
                            INSERT INTO %s (key, value) VALUES ('version', '0')
                            """,
                    tableName
            ));
        }
    }

    @Override
    protected void patchIfNeeded(Connection connection) throws SQLException {
        // TODO Implement patching
        // TODO Get Version of repository and compare with needed version - if mismatch, patch!
    }

    @Override
    protected RepositoryVersion readVersionFromMetaDataTable(Connection connection, String metaDataTableName)
            throws SQLException {
        try (var statement = connection.createStatement()) {
            var resultSet = statement.executeQuery(String.format(
                    """
                            SELECT value FROM %s WHERE key='version'
                            """,
                    metaDataTableName
            ));

            if (!resultSet.next()) {
                throw new RuntimeException("Could not find version in meta data table");
            }

            long version = resultSet.getLong("value");
            return RepositoryVersion.of(version);
        }
    }

    protected <T> Mono<T> executeSqlQueryWithOneResult(
            String sql,
            PreparedStatementConfig statementConfig,
            ResultSetRowMapper<T> resultRowMapper
    ) {
        return executeSqlQuery(sql, statementConfig, resultRowMapper).next();
    }

    protected <T> Flux<T> executeSqlQuery(
            String sql,
            PreparedStatementConfig statementConfig,
            ResultSetRowMapper<T> resultRowMapper
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

    private <T> Flux<T> toFlux(ResultSet resultSet, ResultSetRowMapper<T> rowMapper) {
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

    private Path getFilePath() {
        if (isTemporary) {
            try {
                Path tempDir = Files.createTempDirectory(".author");
                return tempDir.resolve(String.format("%s.%s", name, FILE_EXTENSION));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            Path userHomePath = SystemUtils.getUserHome().toPath();
            Path authorPath = userHomePath.resolve(".author");
            Path dbPath = authorPath.resolve("db");

            return dbPath.resolve(String.format("%s.%s", name, FILE_EXTENSION));
        }
    }

    public interface PreparedStatementConfig {

        void accept(PreparedStatement statement) throws SQLException;

    }

    public interface ResultSetRowMapper<T> {

        T map(ResultSet resultSet) throws SQLException;

    }

}
