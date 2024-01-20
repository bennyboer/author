package de.bennyboer.author.user.mail;

import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.Event;
import de.bennyboer.author.eventsourcing.event.EventName;
import de.bennyboer.author.user.UserEvent;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MailUpdateConfirmedEvent implements Event {

    private static final Version VERSION = Version.zero();

    public static MailUpdateConfirmedEvent of() {
        return new MailUpdateConfirmedEvent();
    }

    @Override
    public EventName getEventName() {
        return UserEvent.MAIL_UPDATE_CONFIRMED.getName();
    }

    @Override
    public Version getVersion() {
        return VERSION;
    }

}
