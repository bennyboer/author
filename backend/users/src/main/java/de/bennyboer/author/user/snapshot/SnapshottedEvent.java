package de.bennyboer.author.user.snapshot;

import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.Event;
import de.bennyboer.author.eventsourcing.event.EventName;
import de.bennyboer.author.eventsourcing.event.SnapshotEvent;
import de.bennyboer.author.user.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SnapshottedEvent implements Event, SnapshotEvent {

    private static final Version VERSION = Version.zero();

    UserName name;

    Mail mail;

    FirstName firstName;

    LastName lastName;

    Password password;

    Instant createdAt;

    public static SnapshottedEvent of(
            UserName name,
            Mail mail,
            FirstName firstName,
            LastName lastName,
            Password password,
            Instant createdAt
    ) {
        checkNotNull(name, "Name must be given");
        checkNotNull(mail, "Mail must be given");
        checkNotNull(firstName, "First name must be given");
        checkNotNull(lastName, "Last name must be given");
        checkNotNull(password, "Password must be given");
        checkNotNull(createdAt, "Created at must be given");

        return new SnapshottedEvent(name, mail, firstName, lastName, password, createdAt);
    }

    @Override
    public EventName getEventName() {
        return UserEvent.SNAPSHOTTED.getName();
    }

    @Override
    public Version getVersion() {
        return VERSION;
    }

}
