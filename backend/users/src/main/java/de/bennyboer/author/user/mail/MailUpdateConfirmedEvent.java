package de.bennyboer.author.user.mail;

import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.Event;
import de.bennyboer.author.eventsourcing.event.EventName;
import de.bennyboer.author.user.Mail;
import de.bennyboer.author.user.UserEvent;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MailUpdateConfirmedEvent implements Event {

    private static final Version VERSION = Version.zero();

    Mail mail;

    public static MailUpdateConfirmedEvent of(Mail mail) {
        checkNotNull(mail, "Mail must be given");

        return new MailUpdateConfirmedEvent(mail);
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
