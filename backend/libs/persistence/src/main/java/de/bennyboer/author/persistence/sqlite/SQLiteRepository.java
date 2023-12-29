package de.bennyboer.author.persistence.sqlite;

import de.bennyboer.author.persistence.Repository;
import de.bennyboer.author.persistence.RepositoryVersion;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.SystemUtils;
import reactor.core.publisher.Mono;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteRepository implements Repository {

    private static final String FILE_EXTENSION = "sqlite3";
    private static final String META_DATA_TABLE_NAME = "meta_data";

    private final String name;

    @Nullable
    private Connection connection;

    public SQLiteRepository(String name) {
        this.name = name;

        try {
            initializeOrPatch();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Mono<RepositoryVersion> getVersion() {
        return Mono.fromCallable(this::getVersionSync);
    }

    @Override
    public void close() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    private void initializeOrPatch() throws SQLException {
        Connection connection = getConnection();

        if (isMetaDataTableMissing(connection)) {
            createMetaDataTable(connection);
        } else {
            patchIfNeeded(connection);
        }
    }

    private boolean isMetaDataTableMissing(Connection connection) {
        return !tableExists(connection, META_DATA_TABLE_NAME);
    }

    private boolean tableExists(Connection connection, String tableName) {
        try (var statement = connection.createStatement()) {
            statement.execute(String.format(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name='%s'",
                    tableName
            ));

            return statement.getResultSet().next();
        } catch (SQLException e) {
            return false;
        }
    }

    private void patchIfNeeded(Connection connection) {
        // TODO Implement patching
        // TODO Get Version of repository and compare with needed version - if mismatch, patch!
    }

    private void createMetaDataTable(Connection connection) {
        try (var statement = connection.createStatement()) {
            statement.execute(String.format(
                    """
                            CREATE TABLE %s (
                                key text PRIMARY KEY,
                                value text NOT NULL
                            )
                            """,
                    META_DATA_TABLE_NAME
            ));

            statement.execute(String.format(
                    """
                            INSERT INTO %s (key, value) VALUES ('version', '0')
                            """,
                    META_DATA_TABLE_NAME
            ));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            createConnection();
        }

        return connection;
    }

    private void createConnection() throws SQLException {
        Path userHomePath = SystemUtils.getUserHome().toPath();
        Path authorPath = userHomePath.resolve("author");
        Path dbPath = authorPath.resolve("db");
        Path dbFile = dbPath.resolve(String.format("%s.%s", name, FILE_EXTENSION));

        String url = String.format("jdbc:sqlite:%s", dbFile.toAbsolutePath());

        this.connection = DriverManager.getConnection(url);
    }

    private RepositoryVersion getVersionSync() {
        try (var statement = getConnection().createStatement()) {
            var resultSet = statement.executeQuery(String.format(
                    """
                            SELECT value FROM %s WHERE key='version'
                            """,
                    META_DATA_TABLE_NAME
            ));

            if (!resultSet.next()) {
                throw new RuntimeException("Could not find version in meta data table");
            }

            long version = resultSet.getLong("value");
            return RepositoryVersion.of(version);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
