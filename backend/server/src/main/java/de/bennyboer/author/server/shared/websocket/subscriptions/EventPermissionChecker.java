package de.bennyboer.author.server.shared.websocket.subscriptions;

import de.bennyboer.author.eventsourcing.aggregate.AggregateId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import reactor.core.publisher.Mono;

public interface EventPermissionChecker {

    AggregateType getAggregateType();

    Mono<Boolean> hasPermissionToReceiveEvents(Agent agent, AggregateId aggregateId);

}
