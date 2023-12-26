package de.bennyboer.author.server.shared.websocket.subscriptions.permissions;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
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

    AggregateType aggregateType;

    @Nullable
    AggregateId aggregateId;

    @Nullable
    UserId userId;

    public static PermissionEventSubscriptionTarget of(
            AggregateType aggregateType,
            @Nullable AggregateId aggregateId,
            @Nullable UserId userId
    ) {
        checkNotNull(aggregateType, "Aggregate type must not be null");

        return new PermissionEventSubscriptionTarget(aggregateType, aggregateId, userId);
    }

    public Optional<AggregateId> getAggregateId() {
        return Optional.ofNullable(aggregateId);
    }

    public Optional<UserId> getUserId() {
        return Optional.ofNullable(userId);
    }

    @Override
    public String toString() {
        return String.format("PermissionEventSubscriptionTarget(%s, %s, %s)", aggregateType, aggregateId, userId);
    }

}
