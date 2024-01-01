package de.bennyboer.author.permissions.repo;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.permissions.*;
import de.bennyboer.author.persistence.sqlite.SQLiteRepository;
import org.sqlite.SQLiteException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

public class SQLitePermissionsRepo extends SQLiteRepository implements PermissionsRepo {

    private static final String NULL_VALUE_PLACEHOLDER = "NONE";

    public SQLitePermissionsRepo(String name) {
        super(name);
    }

    public SQLitePermissionsRepo(String name, boolean isTemporary) {
        super(name, isTemporary);
    }

    @Override
    public Mono<Permission> insert(Permission permission) {
        String sql = """
                INSERT INTO permissions (user_id, action, resource_type, resource_id)
                VALUES (?, ?, ?, ?)
                """;

        return update(sql, statement -> {
            statement.setString(1, permission.getUserId().getValue());
            statement.setString(2, permission.getAction().getName());
            statement.setString(3, permission.getResource().getType().getName());
            statement.setString(
                    4,
                    permission.getResource()
                            .getId()
                            .map(ResourceId::getValue)
                            .orElse(NULL_VALUE_PLACEHOLDER)
            );
        })
                .thenReturn(permission)
                .onErrorResume(e -> {
                    if (e instanceof SQLiteException sqLiteException) {
                        boolean isUniqueConstraintViolation = sqLiteException.getErrorCode() == 19;
                        if (isUniqueConstraintViolation) {
                            return Mono.empty();
                        }
                    }

                    return Mono.error(e);
                });
    }

    @Override
    public Flux<Permission> insertAll(Collection<Permission> permissions) {
        String sql = """
                INSERT INTO permissions (user_id, action, resource_type, resource_id)
                VALUES (?, ?, ?, ?)
                """;

        return batchUpdate(sql, permissions, (statement, permission) -> {
            statement.setString(1, permission.getUserId().getValue());
            statement.setString(2, permission.getAction().getName());
            statement.setString(3, permission.getResource().getType().getName());
            statement.setString(
                    4,
                    permission.getResource()
                            .getId()
                            .map(ResourceId::getValue)
                            .orElse(NULL_VALUE_PLACEHOLDER)
            );
        }).thenMany(Flux.fromIterable(permissions));
    }

    @Override
    public Mono<Boolean> hasPermission(Permission permission) {
        String sql = """
                SELECT * FROM permissions
                WHERE user_id = ?
                AND action = ?
                AND resource_type = ?
                AND resource_id = ?
                LIMIT 1
                """;

        return queryOne(sql, statement -> {
            statement.setString(1, permission.getUserId().getValue());
            statement.setString(2, permission.getAction().getName());
            statement.setString(3, permission.getResource().getType().getName());
            statement.setString(
                    4,
                    permission.getResource().getId().map(ResourceId::getValue).orElse(NULL_VALUE_PLACEHOLDER)
            );
        }, this::readPermissionFromResultSet).hasElement();
    }

    @Override
    public Flux<Permission> findPermissionsByUserId(UserId userId) {
        String sql = """
                SELECT * FROM permissions
                WHERE user_id = ?
                """;

        return query(sql, statement -> {
            statement.setString(1, userId.getValue());
        }, this::readPermissionFromResultSet);
    }

    @Override
    public Flux<Permission> findPermissionsByUserIdAndResourceType(UserId userId, ResourceType resourceType) {
        String sql = """
                SELECT * FROM permissions
                WHERE user_id = ?
                AND resource_type = ?
                """;

        return query(sql, statement -> {
            statement.setString(1, userId.getValue());
            statement.setString(2, resourceType.getName());
        }, this::readPermissionFromResultSet);
    }

    @Override
    public Flux<Permission> findPermissionsByUserIdAndResourceTypeAndAction(
            UserId userId,
            ResourceType resourceType,
            Action action
    ) {
        String sql = """
                SELECT * FROM permissions
                WHERE user_id = ?
                AND resource_type = ?
                AND action = ?
                """;

        return query(sql, statement -> {
            statement.setString(1, userId.getValue());
            statement.setString(2, resourceType.getName());
            statement.setString(3, action.getName());
        }, this::readPermissionFromResultSet);
    }

    @Override
    public Flux<Permission> findPermissionsByUserIdAndResource(UserId userId, Resource resource) {
        String sql = """
                SELECT * FROM permissions
                WHERE user_id = ?
                AND resource_type = ?
                AND resource_id = ?
                """;

        return query(sql, statement -> {
            statement.setString(1, userId.getValue());
            statement.setString(2, resource.getType().getName());
            statement.setString(3, resource.getId().map(ResourceId::getValue).orElse(NULL_VALUE_PLACEHOLDER));
        }, this::readPermissionFromResultSet);
    }

    @Override
    public Flux<Permission> removeByUserId(UserId userId) {
        String deleteSql = """
                DELETE FROM permissions
                WHERE user_id = ?
                RETURNING *
                """;

        return query(deleteSql, statement -> {
            statement.setString(1, userId.getValue());
        }, this::readPermissionFromResultSet);
    }

    @Override
    public Flux<Permission> removeByResource(Resource resource) {
        String deleteSql = """
                DELETE FROM permissions
                WHERE resource_type = ?
                AND resource_id = ?
                RETURNING *
                """;

        return query(deleteSql, statement -> {
            statement.setString(1, resource.getType().getName());
            statement.setString(2, resource.getId().map(ResourceId::getValue).orElse(NULL_VALUE_PLACEHOLDER));
        }, this::readPermissionFromResultSet);
    }

    @Override
    public Flux<Permission> removeByUserIdAndResource(UserId userId, Resource resource) {
        String deleteSql = """
                DELETE FROM permissions
                WHERE user_id = ?
                AND resource_type = ?
                AND resource_id = ?
                RETURNING *
                """;

        return query(deleteSql, statement -> {
            statement.setString(1, userId.getValue());
            statement.setString(2, resource.getType().getName());
            statement.setString(3, resource.getId().map(ResourceId::getValue).orElse(NULL_VALUE_PLACEHOLDER));
        }, this::readPermissionFromResultSet);
    }

    @Override
    public Mono<Permission> removeByPermission(Permission permission) {
        String deleteSql = """
                DELETE FROM permissions
                WHERE user_id = ?
                AND action = ?
                AND resource_type = ?
                AND resource_id = ?
                RETURNING *
                """;

        return queryOne(deleteSql, statement -> {
            statement.setString(1, permission.getUserId().getValue());
            statement.setString(2, permission.getAction().getName());
            statement.setString(3, permission.getResource().getType().getName());
            statement.setString(
                    4,
                    permission.getResource().getId().map(ResourceId::getValue).orElse(NULL_VALUE_PLACEHOLDER)
            );
        }, this::readPermissionFromResultSet);
    }

    @Override
    protected void initialize(Connection connection) throws SQLException {
        createPermissionsTable(connection);
    }

    private void createPermissionsTable(Connection connection) throws SQLException {
        try (var statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE permissions (
                        user_id TEXT NOT NULL,
                        action TEXT NOT NULL,
                        resource_type TEXT NOT NULL,
                        resource_id TEXT,
                        PRIMARY KEY (user_id, action, resource_type, resource_id)
                    )
                    """);
        }
    }

    private Permission readPermissionFromResultSet(ResultSet resultSet) throws SQLException {
        UserId userId = UserId.of(resultSet.getString("user_id"));
        Action action = Action.of(resultSet.getString("action"));

        ResourceType resourceType = ResourceType.of(resultSet.getString("resource_type"));
        ResourceId resourceId = ResourceId.of(resultSet.getString("resource_id"));
        Resource resource = Resource.of(resourceType, resourceId);

        return Permission.builder()
                .user(userId)
                .isAllowedTo(action)
                .on(resource);
    }

}
