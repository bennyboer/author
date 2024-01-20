package de.bennyboer.author.user.password;

import de.bennyboer.author.auth.password.EncodedPassword;
import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.Event;
import de.bennyboer.author.eventsourcing.event.EventName;
import de.bennyboer.author.user.Password;
import de.bennyboer.author.user.UserEvent;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PasswordChangedEvent implements Event {

    private static final Version VERSION = Version.zero();

    Password password;

    public static PasswordChangedEvent of(Password password) {
        checkNotNull(password, "Password must be given");

        EncodedPassword encodedPassword = EncodedPassword.ofRaw(password.getValue());

        return new PasswordChangedEvent(Password.of(encodedPassword.getValue()));
    }

    public static PasswordChangedEvent ofStored(Password password) {
        checkNotNull(password, "Password must be given");

        return new PasswordChangedEvent(password);
    }

    @Override
    public EventName getEventName() {
        return UserEvent.PASSWORD_CHANGED.getName();
    }

    @Override
    public Version getVersion() {
        return VERSION;
    }

}
