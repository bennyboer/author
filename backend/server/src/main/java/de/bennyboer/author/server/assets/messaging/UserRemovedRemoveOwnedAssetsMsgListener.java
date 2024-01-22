package de.bennyboer.author.server.assets.messaging;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.eventsourcing.event.EventName;
import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.author.server.assets.facade.AssetsCommandFacade;
import de.bennyboer.author.server.shared.messaging.events.AggregateEventMessage;
import de.bennyboer.author.server.shared.messaging.events.AggregateEventMessageListener;
import de.bennyboer.author.user.User;
import de.bennyboer.author.user.UserEvent;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Optional;

@AllArgsConstructor
public class UserRemovedRemoveOwnedAssetsMsgListener implements AggregateEventMessageListener {

    private final AssetsCommandFacade commandFacade;

    @Override
    public AggregateType aggregateType() {
        return User.TYPE;
    }

    @Override
    public Optional<EventName> eventName() {
        return Optional.of(UserEvent.REMOVED.getName());
    }

    @Override
    public Mono<Void> onMessage(AggregateEventMessage message) {
        UserId userId = UserId.of(message.getAggregateId());
        Agent agent = message.getUserId()
                .map(uId -> Agent.user(UserId.of(uId)))
                .orElse(Agent.system());

        return commandFacade.removeOwnedAssets(userId, agent);
    }

}
