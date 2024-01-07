package de.bennyboer.author.user.create;

import de.bennyboer.author.auth.password.EncodedPassword;
import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.Event;
import de.bennyboer.author.eventsourcing.event.EventName;
import de.bennyboer.author.user.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CreatedEvent implements Event {

    private static final Version VERSION = Version.zero();

    UserName name;

    Mail mail;

    FirstName firstName;

    LastName lastName;

    Password password;

    public static CreatedEvent of(UserName name, Mail mail, FirstName firstName, LastName lastName, Password password) {
        checkNotNull(name, "Name must be given");
        checkNotNull(mail, "Mail must be given");
        checkNotNull(firstName, "First name must be given");
        checkNotNull(lastName, "Last name must be given");
        checkNotNull(password, "Password must be given");

        EncodedPassword encodedPassword = EncodedPassword.ofRaw(password.getValue());

        return new CreatedEvent(name, mail, firstName, lastName, Password.of(encodedPassword.getValue()));
    }

    public static CreatedEvent ofStored(
            UserName name,
            Mail mail,
            FirstName firstName,
            LastName lastName,
            Password password
    ) {
        checkNotNull(name, "Name must be given");
        checkNotNull(mail, "Mail must be given");
        checkNotNull(firstName, "First name must be given");
        checkNotNull(lastName, "Last name must be given");
        checkNotNull(password, "Password must be given");

        return new CreatedEvent(name, mail, firstName, lastName, password);
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
