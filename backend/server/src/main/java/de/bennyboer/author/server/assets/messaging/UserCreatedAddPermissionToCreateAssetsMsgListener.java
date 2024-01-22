package de.bennyboer.author.server.assets.messaging;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.eventsourcing.event.EventName;
import de.bennyboer.author.server.assets.facade.AssetsPermissionsFacade;
import de.bennyboer.author.server.shared.messaging.events.AggregateEventMessage;
import de.bennyboer.author.server.shared.messaging.events.AggregateEventMessageListener;
import de.bennyboer.author.user.User;
import de.bennyboer.author.user.UserEvent;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Optional;

@AllArgsConstructor
public class UserCreatedAddPermissionToCreateAssetsMsgListener implements AggregateEventMessageListener {

    private final AssetsPermissionsFacade permissionsFacade;

    @Override
    public AggregateType aggregateType() {
        return User.TYPE;
    }

    @Override
    public Optional<EventName> eventName() {
        return Optional.of(UserEvent.CREATED.getName());
    }

    @Override
    public Mono<Void> onMessage(AggregateEventMessage message) {
        UserId userId = UserId.of(message.getAggregateId());

        return permissionsFacade.addPermissionToCreateAssetsForNewUser(userId);
    }

}
