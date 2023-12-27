package de.bennyboer.author.permissions;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.permissions.event.PermissionEvent;
import de.bennyboer.author.permissions.repo.InMemoryPermissionsRepo;
import de.bennyboer.author.permissions.repo.PermissionsRepo;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class PermissionsServiceTests {

    Set<PermissionEvent> seenEvents = new HashSet<>();
    PermissionsRepo permissionsRepo = new InMemoryPermissionsRepo();

    PermissionsService service = new PermissionsService(permissionsRepo, event -> {
        seenEvents.add(event);
        return Mono.empty();
    });

    UserId userId = UserId.of("USER_ID");
    ResourceType resourceType = ResourceType.of("RESOURCE_TYPE");
    ResourceId resourceId = ResourceId.of("RESOURCE_ID");
    Resource resource = Resource.of(resourceType, resourceId);
    Action testAction = Action.of("TEST_ACTION");

    @Test
    void shouldAddPermission() {
        // given: a permission to add
        Permission permission = Permission.builder()
                .user(userId)
                .isAllowedTo(testAction)
                .on(resource);

        // when: adding the permission
        service.addPermission(permission).block();

        // then: the permission is added
        assertTrue(service.hasPermission(permission).block());

        // and: an event is published
        assertTrue(seenEvents.contains(PermissionEvent.added(permission)));
    }

    @Test
    void shouldNotHavePermission() {
        // given: a permission to check
        Permission permission = Permission.builder()
                .user(userId)
                .isAllowedTo(testAction)
                .on(resource);

        // when: checking the permission
        boolean hasPermission = service.hasPermission(permission).block();

        // then: the permission is not present
        assertFalse(hasPermission);
    }

    @Test
    void shouldNotAllowHavingAPermissionTwice() {
        // given: a permission to add
        Permission permission = Permission.builder()
                .user(userId)
                .isAllowedTo(testAction)
                .on(resource);

        // and: the permission is added
        service.addPermission(permission).block();

        // when: adding the permission again
        service.addPermission(permission).block();

        // then: the permission is still present only once
        assertEquals(1, service.findPermissionsByUserId(userId).collectList().block().size());
    }

    @Test
    void shouldHavePermissionOnNoSpecificResource() {
        // given: a permission on no specific resource
        Permission permission = Permission.builder()
                .user(userId)
                .isAllowedTo(testAction)
                .onType(resourceType);

        service.addPermission(permission).block();

        // when: checking the permission
        boolean hasPermission = service.hasPermission(permission).block();

        // then: the permission is present
        assertTrue(hasPermission);

        // when: checking the permission for a specific resource
        hasPermission = service.hasPermission(Permission.builder()
                .user(userId)
                .isAllowedTo(testAction)
                .on(resource)).block();

        // then: the permission is not present
        assertFalse(hasPermission);
    }

    @Test
    void shouldAddMultiplePermissions() {
        var userId1 = UserId.of("USER_ID_1");
        var userId2 = UserId.of("USER_ID_2");

        var resource1 = Resource.of(ResourceType.of("RESOURCE_TYPE_1"), ResourceId.of("RESOURCE_ID_1"));
        var resource2 = Resource.of(ResourceType.of("RESOURCE_TYPE_2"), ResourceId.of("RESOURCE_ID_2"));

        // given: a set of permissions to add
        Set<Permission> permissions = Set.of(
                Permission.builder()
                        .user(userId1)
                        .isAllowedTo(testAction)
                        .on(resource1),
                Permission.builder()
                        .user(userId1)
                        .isAllowedTo(testAction)
                        .on(resource2),
                Permission.builder()
                        .user(userId2)
                        .isAllowedTo(testAction)
                        .on(resource2)
        );

        // when: adding the permissions
        service.addPermissions(permissions).block();

        // then: user 1 has permissions to perform the test action on resource 1 and 2
        assertTrue(service.hasPermission(Permission.builder()
                .user(userId1)
                .isAllowedTo(testAction)
                .on(resource1)).block());
        assertTrue(service.hasPermission(Permission.builder()
                .user(userId1)
                .isAllowedTo(testAction)
                .on(resource2)).block());

        // and: user 2 has permissions to perform the test action on resource 2 but not on resource 1
        assertTrue(service.hasPermission(Permission.builder()
                .user(userId2)
                .isAllowedTo(testAction)
                .on(resource2)).block());
        assertFalse(service.hasPermission(Permission.builder()
                .user(userId2)
                .isAllowedTo(testAction)
                .on(resource1)).block());

        // and: an event is published
        assertTrue(seenEvents.contains(PermissionEvent.added(permissions)));
    }

    @Test
    void shouldFindPermissionsByUserId() {
        var userId1 = UserId.of("USER_ID_1");
        var userId2 = UserId.of("USER_ID_2");

        var resource1 = Resource.of(ResourceType.of("RESOURCE_TYPE_1"), ResourceId.of("RESOURCE_ID_1"));
        var resource2 = Resource.of(ResourceType.of("RESOURCE_TYPE_2"), ResourceId.of("RESOURCE_ID_2"));

        // given: a set of permissions to add
        Set<Permission> permissions = Set.of(
                Permission.builder()
                        .user(userId1)
                        .isAllowedTo(testAction)
                        .on(resource1),
                Permission.builder()
                        .user(userId1)
                        .isAllowedTo(testAction)
                        .on(resource2),
                Permission.builder()
                        .user(userId2)
                        .isAllowedTo(testAction)
                        .on(resource2)
        );

        // and: the permissions are added
        service.addPermissions(permissions).block();

        // when: finding permissions for user 1
        var foundPermissions = service.findPermissionsByUserId(userId1).collectList().block();

        // then: the permissions for user 1 are found
        assertEquals(2, foundPermissions.size());
        assertTrue(foundPermissions.containsAll(List.of(
                Permission.builder()
                        .user(userId1)
                        .isAllowedTo(testAction)
                        .on(resource1),
                Permission.builder()
                        .user(userId1)
                        .isAllowedTo(testAction)
                        .on(resource2)
        )));

        // when: finding permissions for user 2
        foundPermissions = service.findPermissionsByUserId(userId2).collectList().block();

        // then: the permissions for user 2 are found
        assertEquals(1, foundPermissions.size());
        assertTrue(foundPermissions.containsAll(List.of(
                Permission.builder()
                        .user(userId2)
                        .isAllowedTo(testAction)
                        .on(resource2)
        )));
    }

    @Test
    void shouldFindPermissionsByUserIdAndResourceType() {
        var userId1 = UserId.of("USER_ID_1");
        var userId2 = UserId.of("USER_ID_2");

        var resource1 = Resource.of(ResourceType.of("RESOURCE_TYPE_1"), ResourceId.of("RESOURCE_ID_1"));
        var resource2 = Resource.of(ResourceType.of("RESOURCE_TYPE_2"), ResourceId.of("RESOURCE_ID_2"));
        var resource3 = Resource.of(ResourceType.of("RESOURCE_TYPE_1"), ResourceId.of("RESOURCE_ID_3"));

        // given: some permissions
        Set<Permission> permissions = Set.of(
                Permission.builder()
                        .user(userId1)
                        .isAllowedTo(testAction)
                        .on(resource1),
                Permission.builder()
                        .user(userId1)
                        .isAllowedTo(testAction)
                        .on(resource2),
                Permission.builder()
                        .user(userId2)
                        .isAllowedTo(testAction)
                        .on(resource2),
                Permission.builder()
                        .user(userId2)
                        .isAllowedTo(testAction)
                        .on(resource3)
        );

        service.addPermissions(permissions).block();

        // when: finding permissions for user 1 and resource type 1
        var foundPermissions = service.findPermissionsByUserIdAndResourceType(
                userId1,
                ResourceType.of("RESOURCE_TYPE_1")
        ).collectList().block();

        // then: the permissions for user 1 and resource type 1 are found
        assertEquals(1, foundPermissions.size());
        assertTrue(foundPermissions.containsAll(List.of(
                Permission.builder()
                        .user(userId1)
                        .isAllowedTo(testAction)
                        .on(resource1)
        )));

        // when: finding permissions for user 2 and resource type 1
        foundPermissions = service.findPermissionsByUserIdAndResourceType(userId2, ResourceType.of("RESOURCE_TYPE_1"))
                .collectList()
                .block();

        // then: the permissions for user 2 and resource type 1 are found
        assertEquals(1, foundPermissions.size());
        assertTrue(foundPermissions.containsAll(List.of(
                Permission.builder()
                        .user(userId2)
                        .isAllowedTo(testAction)
                        .on(resource3)
        )));
    }

    @Test
    void shouldFindPermissionsByUserIdAndResourceTypeAndAction() {
        var userId1 = UserId.of("USER_ID_1");
        var userId2 = UserId.of("USER_ID_2");

        var resource1 = Resource.of(ResourceType.of("RESOURCE_TYPE_1"), ResourceId.of("RESOURCE_ID_1"));
        var resource2 = Resource.of(ResourceType.of("RESOURCE_TYPE_2"), ResourceId.of("RESOURCE_ID_2"));
        var resource3 = Resource.of(ResourceType.of("RESOURCE_TYPE_1"), ResourceId.of("RESOURCE_ID_3"));

        var action1 = Action.of("ACTION_1");
        var action2 = Action.of("ACTION_2");

        // given: some permissions
        Set<Permission> permissions = Set.of(
                Permission.builder()
                        .user(userId1)
                        .isAllowedTo(action1)
                        .on(resource1),
                Permission.builder()
                        .user(userId1)
                        .isAllowedTo(action2)
                        .on(resource2),
                Permission.builder()
                        .user(userId2)
                        .isAllowedTo(action2)
                        .on(resource2),
                Permission.builder()
                        .user(userId2)
                        .isAllowedTo(action1)
                        .on(resource3)
        );

        service.addPermissions(permissions).block();

        // when: finding permissions for user 1 and resource type 1 and action 1
        var foundPermissions = service.findPermissionsByUserIdAndResourceTypeAndAction(
                userId1,
                ResourceType.of("RESOURCE_TYPE_1"),
                action1
        ).collectList().block();

        // then: the permissions for user 1 and resource type 1 and action 1 are found
        assertEquals(1, foundPermissions.size());
        assertTrue(foundPermissions.containsAll(List.of(
                Permission.builder()
                        .user(userId1)
                        .isAllowedTo(action1)
                        .on(resource1)
        )));
    }

    @Test
    void shouldFindPermissionsByUserIdAndResource() {
        var userId1 = UserId.of("USER_ID_1");
        var userId2 = UserId.of("USER_ID_2");

        var resource1 = Resource.of(ResourceType.of("RESOURCE_TYPE_1"), ResourceId.of("RESOURCE_ID_1"));
        var resource2 = Resource.of(ResourceType.of("RESOURCE_TYPE_2"), ResourceId.of("RESOURCE_ID_2"));
        var resource3 = Resource.of(ResourceType.of("RESOURCE_TYPE_1"), ResourceId.of("RESOURCE_ID_3"));

        // given: some permissions
        Set<Permission> permissions = Set.of(
                Permission.builder()
                        .user(userId1)
                        .isAllowedTo(testAction)
                        .on(resource1),
                Permission.builder()
                        .user(userId1)
                        .isAllowedTo(testAction)
                        .on(resource2),
                Permission.builder()
                        .user(userId2)
                        .isAllowedTo(testAction)
                        .on(resource2),
                Permission.builder()
                        .user(userId2)
                        .isAllowedTo(testAction)
                        .on(resource3)
        );

        service.addPermissions(permissions).block();

        // when: finding permissions for user 1 and resource 1
        var foundPermissions = service.findPermissionsByUserIdAndResource(userId1, resource1)
                .collectList()
                .block();

        // then: the permissions for user 1 and resource 1 are found
        assertEquals(1, foundPermissions.size());
        assertTrue(foundPermissions.containsAll(List.of(
                Permission.builder()
                        .user(userId1)
                        .isAllowedTo(testAction)
                        .on(resource1)
        )));

        // when: finding permissions for user 2 and resource 1
        foundPermissions = service.findPermissionsByUserIdAndResource(userId2, resource1)
                .collectList()
                .block();

        // then: no permissions for user 2 and resource 1 are found
        assertTrue(foundPermissions.isEmpty());

        // when: finding permissions for user 2 and resource 2
        foundPermissions = service.findPermissionsByUserIdAndResource(userId2, resource2)
                .collectList()
                .block();

        // then: the permissions for user 2 and resource 2 are found
        assertEquals(1, foundPermissions.size());
        assertTrue(foundPermissions.containsAll(List.of(
                Permission.builder()
                        .user(userId2)
                        .isAllowedTo(testAction)
                        .on(resource2)
        )));
    }

    @Test
    void shouldRemovePermission() {
        // given: a permission to remove
        Permission permission = Permission.builder()
                .user(userId)
                .isAllowedTo(testAction)
                .on(resource);

        // and: the permission is added
        service.addPermission(permission).block();

        // when: removing the permission
        service.removePermission(permission).block();

        // then: the permission is removed
        assertFalse(service.hasPermission(permission).block());

        // and: an event is published
        assertTrue(seenEvents.contains(PermissionEvent.removed(permission)));
    }

    @Test
    void shouldNotPublishEventWhenPermissionToBeRemovedIsAbsent() {
        // given: a permission to remove
        Permission permission = Permission.builder()
                .user(userId)
                .isAllowedTo(testAction)
                .on(resource);

        // when: removing the permission
        service.removePermission(permission).block();

        // then: no event is published
        assertTrue(seenEvents.isEmpty());
    }

    @Test
    void shouldRemovePermissionsByUserId() {
        var userId1 = UserId.of("USER_ID_1");
        var userId2 = UserId.of("USER_ID_2");

        var resource1 = Resource.of(ResourceType.of("RESOURCE_TYPE_1"), ResourceId.of("RESOURCE_ID_1"));
        var resource2 = Resource.of(ResourceType.of("RESOURCE_TYPE_2"), ResourceId.of("RESOURCE_ID_2"));

        // given: some permissions
        Set<Permission> permissions = Set.of(
                Permission.builder()
                        .user(userId1)
                        .isAllowedTo(testAction)
                        .on(resource1),
                Permission.builder()
                        .user(userId1)
                        .isAllowedTo(testAction)
                        .on(resource2),
                Permission.builder()
                        .user(userId2)
                        .isAllowedTo(testAction)
                        .on(resource2)
        );

        service.addPermissions(permissions).block();

        // when: removing permissions for user 1
        service.removePermissionsByUserId(userId1).block();

        // then: the permissions for user 1 are removed
        assertFalse(service.hasPermission(Permission.builder()
                .user(userId1)
                .isAllowedTo(testAction)
                .on(resource1)).block());
        assertFalse(service.hasPermission(Permission.builder()
                .user(userId1)
                .isAllowedTo(testAction)
                .on(resource2)).block());

        // and: the permissions for user 2 are still present
        assertTrue(service.hasPermission(Permission.builder()
                .user(userId2)
                .isAllowedTo(testAction)
                .on(resource2)).block());

        // and: an event is published
        assertTrue(seenEvents.contains(PermissionEvent.removed(Set.of(
                Permission.builder()
                        .user(userId1)
                        .isAllowedTo(testAction)
                        .on(resource1),
                Permission.builder()
                        .user(userId1)
                        .isAllowedTo(testAction)
                        .on(resource2)
        ))));
    }

    @Test
    void shouldRemovePermissionsByResource() {
        var userId1 = UserId.of("USER_ID_1");
        var userId2 = UserId.of("USER_ID_2");

        var resource1 = Resource.of(ResourceType.of("RESOURCE_TYPE_1"), ResourceId.of("RESOURCE_ID_1"));
        var resource2 = Resource.of(ResourceType.of("RESOURCE_TYPE_2"), ResourceId.of("RESOURCE_ID_2"));

        // given: some permissions
        Set<Permission> permissions = Set.of(
                Permission.builder()
                        .user(userId1)
                        .isAllowedTo(testAction)
                        .on(resource1),
                Permission.builder()
                        .user(userId1)
                        .isAllowedTo(testAction)
                        .on(resource2),
                Permission.builder()
                        .user(userId2)
                        .isAllowedTo(testAction)
                        .on(resource2)
        );

        service.addPermissions(permissions).block();

        // when: removing permissions for resource 2
        service.removePermissionsByResource(resource2).block();

        // then: the permissions for resource 2 are removed
        assertFalse(service.hasPermission(Permission.builder()
                .user(userId1)
                .isAllowedTo(testAction)
                .on(resource2)).block());

        assertFalse(service.hasPermission(Permission.builder()
                .user(userId2)
                .isAllowedTo(testAction)
                .on(resource2)).block());

        // and: the permissions for resource 1 are still present
        assertTrue(service.hasPermission(Permission.builder()
                .user(userId1)
                .isAllowedTo(testAction)
                .on(resource1)).block());

        // and: an event is published
        assertTrue(seenEvents.contains(PermissionEvent.removed(Set.of(
                Permission.builder()
                        .user(userId1)
                        .isAllowedTo(testAction)
                        .on(resource2),
                Permission.builder()
                        .user(userId2)
                        .isAllowedTo(testAction)
                        .on(resource2)
        ))));
    }

    @Test
    void shouldRemovePermissionsByUserIdAndResource() {
        var userId1 = UserId.of("USER_ID_1");
        var userId2 = UserId.of("USER_ID_2");

        var resource1 = Resource.of(ResourceType.of("RESOURCE_TYPE_1"), ResourceId.of("RESOURCE_ID_1"));
        var resource2 = Resource.of(ResourceType.of("RESOURCE_TYPE_2"), ResourceId.of("RESOURCE_ID_2"));

        // given: some permissions
        Set<Permission> permissions = Set.of(
                Permission.builder()
                        .user(userId1)
                        .isAllowedTo(testAction)
                        .on(resource1),
                Permission.builder()
                        .user(userId1)
                        .isAllowedTo(testAction)
                        .on(resource2),
                Permission.builder()
                        .user(userId2)
                        .isAllowedTo(testAction)
                        .on(resource2)
        );

        service.addPermissions(permissions).block();

        // when: removing permissions for user 1 and resource 2
        service.removePermissionsByUserIdAndResource(userId1, resource2).block();

        // then: the permissions for user 1 and resource 2 are removed
        assertFalse(service.hasPermission(Permission.builder()
                .user(userId1)
                .isAllowedTo(testAction)
                .on(resource2)).block());

        // and: the permissions for user 1 and resource 1 are still present
        assertTrue(service.hasPermission(Permission.builder()
                .user(userId1)
                .isAllowedTo(testAction)
                .on(resource1)).block());

        // and: the permissions for user 2 and resource 2 are still present
        assertTrue(service.hasPermission(Permission.builder()
                .user(userId2)
                .isAllowedTo(testAction)
                .on(resource2)).block());

        // and: an event is published
        assertTrue(seenEvents.contains(PermissionEvent.removed(Set.of(
                Permission.builder()
                        .user(userId1)
                        .isAllowedTo(testAction)
                        .on(resource2)
        ))));
    }

}
