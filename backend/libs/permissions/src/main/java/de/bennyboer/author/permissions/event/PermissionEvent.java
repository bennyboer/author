package de.bennyboer.author.permissions.event;

import de.bennyboer.author.permissions.Permission;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Collection;
import java.util.List;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PermissionEvent {

    PermissionEventType type;

    Collection<Permission> permissions;

    public static PermissionEvent added(Permission permission) {
        return added(List.of(permission));
    }

    public static PermissionEvent added(Collection<Permission> permissions) {
        return new PermissionEvent(PermissionEventType.ADDED, permissions);
    }

    public static PermissionEvent removed(Permission permission) {
        return removed(List.of(permission));
    }

    public static PermissionEvent removed(Collection<Permission> permissions) {
        return new PermissionEvent(PermissionEventType.REMOVED, permissions);
    }

}
