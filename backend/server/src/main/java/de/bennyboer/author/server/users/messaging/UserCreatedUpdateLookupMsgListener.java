package de.bennyboer.author.server.users.messaging;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.eventsourcing.event.EventName;
import de.bennyboer.author.server.shared.messaging.events.AggregateEventMessage;
import de.bennyboer.author.server.shared.messaging.events.AggregateEventMessageListener;
import de.bennyboer.author.server.users.facade.UsersSyncFacade;
import de.bennyboer.author.user.User;
import de.bennyboer.author.user.create.CreatedEvent;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Optional;

@AllArgsConstructor
public class UserCreatedUpdateLookupMsgListener implements AggregateEventMessageListener {

    private final UsersSyncFacade syncFacade;

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

        return syncFacade.updateUserLookupById(userId);
    }

}
