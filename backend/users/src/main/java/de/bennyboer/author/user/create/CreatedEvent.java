package de.bennyboer.author.user.create;

import de.bennyboer.author.auth.password.EncodedPassword;
import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.Event;
import de.bennyboer.author.eventsourcing.event.EventName;
import de.bennyboer.author.user.Password;
import de.bennyboer.author.user.UserEvent;
import de.bennyboer.author.user.UserName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CreatedEvent implements Event {

    private static final Version VERSION = Version.zero();

    UserName name;

    Password password;

    public static CreatedEvent of(UserName name, Password password) {
        checkNotNull(name, "Name must be given");
        checkNotNull(password, "Password must be given");

        EncodedPassword encodedPassword = EncodedPassword.ofRaw(password.getValue());

        return new CreatedEvent(name, Password.of(encodedPassword.getValue()));
    }

    public static CreatedEvent ofStored(UserName name, Password password) {
        checkNotNull(name, "Name must be given");
        checkNotNull(password, "Password must be given");

        return new CreatedEvent(name, password);
    }

    @Override
    public EventName getEventName() {
        return UserEvent.CREATED.getName();
    }

    @Override
    public Version getVersion() {
        return VERSION;
    }

}
