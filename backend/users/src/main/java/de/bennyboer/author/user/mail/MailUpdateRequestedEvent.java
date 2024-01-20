package de.bennyboer.author.user.mail;

import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.Event;
import de.bennyboer.author.eventsourcing.event.EventName;
import de.bennyboer.author.user.Mail;
import de.bennyboer.author.user.MailConfirmationToken;
import de.bennyboer.author.user.UserEvent;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MailUpdateRequestedEvent implements Event {

    private static final Version VERSION = Version.zero();

    Mail mail;

    MailConfirmationToken token;

    public static MailUpdateRequestedEvent of(Mail mail, MailConfirmationToken token) {
        checkNotNull(mail, "Mail must be given");
        checkNotNull(token, "Mail confirmation token must be given");

        return new MailUpdateRequestedEvent(mail, token);
    }

    @Override
    public EventName getEventName() {
        return UserEvent.MAIL_UPDATE_REQUESTED.getName();
    }

    @Override
    public Version getVersion() {
        return VERSION;
    }

}
