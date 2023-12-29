package de.bennyboer.author.permissions;

import de.bennyboer.author.common.UserId;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

/**
 * A permission is a relation between a user and a specific resource.
 * More precisely it defines an action a user is allowed to perform on a given resource.
 */
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Permission {

    UserId userId;

    Action action;

    Resource resource;

    private static Permission of(UserId userId, Action action, Resource resource) {
        checkNotNull(userId, "User ID must be given");
        checkNotNull(action, "Action must be given");
        checkNotNull(resource, "Resource must be given");

        return new Permission(userId, action, resource);
    }

    @Override
    public String toString() {
        return String.format("Permission(user=%s, action=%s, resource=%s)", userId, action, resource);
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * The preferred way to create a permission.
     * <pre>
     * var permission = Permission.builder()
     *                            .user(userId)
     *                            .isAllowedTo(ADD_NODES)
     *                            .on(structureResource);
     * </pre>
     */
    public static class Builder {
        private UserId userId;
        private Action action;

        public Builder user(UserId userId) {
            this.userId = userId;
            return this;
        }

        public Builder isAllowedTo(Action action) {
            this.action = action;
            return this;
        }

        public Permission onType(ResourceType resourceType) {
            return Permission.of(userId, action, Resource.ofType(resourceType));
        }

        public Permission on(Resource resource) {
            return Permission.of(userId, action, resource);
        }

    }

}
