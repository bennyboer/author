package de.bennyboer.author.server.shared.modules;

import de.bennyboer.author.server.shared.messaging.Messaging;
import de.bennyboer.author.server.shared.messaging.MessagingEventPublisher;
import de.bennyboer.author.eventsourcing.persistence.EventSourcingRepo;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ModuleConfig {

    EventSourcingRepo eventSourcingRepo;

    MessagingEventPublisher eventPublisher;

    Messaging messaging;

    public static ModuleConfig of(
            EventSourcingRepo eventSourcingRepo,
            MessagingEventPublisher eventPublisher,
            Messaging messaging
    ) {
        checkNotNull(eventSourcingRepo, "Event sourcing repository must be given");
        checkNotNull(eventPublisher, "Event publisher must be given");
        checkNotNull(messaging, "Messaging must be given");

        return new ModuleConfig(eventSourcingRepo, eventPublisher, messaging);
    }

}
