package de.bennyboer.author.server.users.messaging;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.server.shared.messaging.AggregateEventMessageListener;
import de.bennyboer.author.server.shared.messaging.messages.AggregateEventMessage;
import de.bennyboer.author.server.users.facade.UsersFacade;
import de.bennyboer.author.user.User;
import de.bennyboer.author.user.create.CreatedEvent;
import de.bennyboer.eventsourcing.aggregate.AggregateType;
import de.bennyboer.eventsourcing.event.EventName;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Optional;

@AllArgsConstructor
public class UserCreatedUpdateLookupMsgListener implements AggregateEventMessageListener {

    private final UsersFacade facade;

    @Override
    public AggregateType aggregateType() {
        return User.TYPE;
    }

    @Override
    public Optional<EventName> eventName() {
        return Optional.of(CreatedEvent.NAME);
    }

    @Override
    public Mono<Void> onMessage(AggregateEventMessage message) {
        UserId userId = UserId.of(message.getAggregateId());

        return facade.updateUserLookupById(userId);
    }

}
