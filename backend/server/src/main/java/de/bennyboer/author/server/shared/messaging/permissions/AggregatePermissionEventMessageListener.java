package de.bennyboer.author.server.shared.messaging.permissions;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.permissions.Action;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface AggregatePermissionEventMessageListener {

    AggregateType aggregateType();

    default Optional<AggregateId> aggregateId() {
        return Optional.empty();
    }

    default Optional<Action> action() {
        return Optional.empty();
    }

    default Optional<UserId> userId() {
        return Optional.empty();
    }

    Mono<Void> onMessage(AggregatePermissionEventMessage message);

}
