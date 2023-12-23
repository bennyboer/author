package de.bennyboer.author.user.create;

import de.bennyboer.author.auth.password.EncodedPassword;
import de.bennyboer.author.user.Password;
import de.bennyboer.author.user.UserName;
import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.Event;
import de.bennyboer.author.eventsourcing.event.EventName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CreatedEvent implements Event {

    public static final EventName NAME = EventName.of("CREATED");

    public static final Version VERSION = Version.zero();

    UserName name;

    Password password;

    public static CreatedEvent of(CreateCmd cmd) {
        EncodedPassword encodedPassword = EncodedPassword.ofRaw(cmd.getPassword().getValue());

        return new CreatedEvent(cmd.getName(), Password.of(encodedPassword.getValue()));
    }

    @Override
    public EventName getEventName() {
        return NAME;
    }

    @Override
    public Version getVersion() {
        return VERSION;
    }

}
