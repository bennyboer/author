package de.bennyboer.eventsourcing.aggregate;

import de.bennyboer.common.UserId;
import de.bennyboer.eventsourcing.EventSourcingService;
import de.bennyboer.eventsourcing.Version;
import de.bennyboer.eventsourcing.command.Command;
import de.bennyboer.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.eventsourcing.event.metadata.agent.AgentType;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public abstract class AggregateService<A extends Aggregate, ID> {

    private final EventSourcingService<A> eventSourcingService;

    public Mono<A> get(ID id) {
        return eventSourcingService.aggregateLatest(toAggregateId(id))
                .filter(aggregate -> !isRemoved(aggregate));
    }

    public Mono<A> get(ID id, Version version) {
        return eventSourcingService.aggregate(toAggregateId(id), version)
                .filter(aggregate -> !isRemoved(aggregate));
    }

    protected abstract AggregateId toAggregateId(ID id);

    protected abstract boolean isRemoved(A aggregate);

    protected Mono<Version> dispatchCommand(ID id, Version version, UserId userId, Command cmd) {
        var agent = Agent.of(AgentType.USER, AgentId.of(userId.getValue()));

        return eventSourcingService.dispatchCommand(toAggregateId(id), version, cmd, agent);
    }

    protected Mono<Version> dispatchCommandToLatest(ID id, UserId userId, Command cmd) {
        var agent = Agent.of(AgentType.USER, AgentId.of(userId.getValue()));

        return eventSourcingService.dispatchCommandToLatest(toAggregateId(id), cmd, agent);
    }

}
