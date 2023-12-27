package de.bennyboer.author.server.shared.websocket.subscriptions.permissions;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.permissions.Action;
import jakarta.annotation.Nullable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.Optional;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Jacksonized
public class PermissionEventSubscriptionTarget {

    UserId userId;

    AggregateType aggregateType;

    @Nullable
    AggregateId aggregateId;

    @Nullable
    Action action;

    public static PermissionEventSubscriptionTarget of(
            UserId userId,
            AggregateType aggregateType,
            @Nullable AggregateId aggregateId,
            @Nullable Action action
    ) {
        checkNotNull(aggregateType, "Aggregate type must not be null");

        return new PermissionEventSubscriptionTarget(userId, aggregateType, aggregateId, action);
    }

    public Optional<AggregateId> getAggregateId() {
        return Optional.ofNullable(aggregateId);
    }

    public Optional<Action> getAction() {
        return Optional.ofNullable(action);
    }

    @Override
    public String toString() {
        return String.format(
                "PermissionEventSubscriptionTarget(%s, %s, %s, %s)",
                userId,
                aggregateType,
                aggregateId,
                action
        );
    }

}
