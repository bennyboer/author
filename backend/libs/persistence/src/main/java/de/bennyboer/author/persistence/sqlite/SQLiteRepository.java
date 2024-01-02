package de.bennyboer.author.persistence.sqlite;

import de.bennyboer.author.persistence.RepositoryVersion;
import de.bennyboer.author.persistence.jdbc.JDBCRepository;
import de.bennyboer.author.persistence.patches.JDBCRepositoryPatch;
import de.bennyboer.author.persistence.patches.PatchManager;
import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteRepository extends JDBCRepository {

    private static final String FILE_EXTENSION = "sqlite3";

    private final String name;
    private final boolean isTemporary;

    public SQLiteRepository(String name, PatchManager<JDBCRepositoryPatch> patchManager, boolean isTemporary) {
        super(patchManager);

        this.name = name;
        this.isTemporary = isTemporary;

        try {
            initializeOrPatch();
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public SQLiteRepository(String name, boolean isTemporary) {
        this(name, new PatchManager<>(), isTemporary);
    }

    public SQLiteRepository(String name) {
        this(name, false);
    }

    @Override
    protected void initialize(Connection connection) throws SQLException {
    }

    @Override
    protected Connection createConnection() throws SQLException, IOException {
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

    private Path getFilePath() throws IOException {
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

            Files.createDirectories(dbPath);

            return dbPath.resolve(String.format("%s.%s", name, FILE_EXTENSION));
        }
    }

}
