package de.bennyboer.author.persistence.jdbc;

import de.bennyboer.author.persistence.Repository;
import de.bennyboer.author.persistence.RepositoryVersion;
import jakarta.annotation.Nullable;
import reactor.core.publisher.Mono;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class JDBCRepository implements Repository {

    private static final String META_DATA_TABLE_NAME = "meta_data";

    @Nullable
    private Connection connection;

    protected abstract Connection createConnection() throws SQLException;

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

    protected void initializeOrPatch() throws SQLException {
        Connection connection = getConnection();

        if (isMetaDataTableMissing(connection)) {
            initializeMetaDataTable(connection, META_DATA_TABLE_NAME);
            initialize(connection);
        } else {
            patchIfNeeded(connection);
        }
    }

    protected Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = createConnection();
        }

        return connection;
    }

    private boolean isMetaDataTableMissing(Connection connection) throws SQLException {
        return !tableExists(connection, META_DATA_TABLE_NAME);
    }

}
