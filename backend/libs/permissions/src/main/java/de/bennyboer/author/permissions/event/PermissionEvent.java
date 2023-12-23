package de.bennyboer.author.permissions.event;

import de.bennyboer.author.permissions.Permission;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Set;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PermissionEvent {

    PermissionEventType type;

    Set<Permission> permissions;

    public static PermissionEvent added(Permission permission) {
        return added(Set.of(permission));
    }

    public static PermissionEvent added(Set<Permission> permissions) {
        return new PermissionEvent(PermissionEventType.ADDED, permissions);
    }

    public static PermissionEvent removed(Permission permission) {
        return removed(Set.of(permission));
    }

    public static PermissionEvent removed(Set<Permission> permissions) {
        return new PermissionEvent(PermissionEventType.REMOVED, permissions);
    }

}
