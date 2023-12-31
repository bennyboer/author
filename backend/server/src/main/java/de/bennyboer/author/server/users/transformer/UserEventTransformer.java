package de.bennyboer.author.server.users.transformer;

import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.Event;
import de.bennyboer.author.eventsourcing.event.EventName;
import de.bennyboer.author.user.Password;
import de.bennyboer.author.user.UserEvent;
import de.bennyboer.author.user.UserName;
import de.bennyboer.author.user.create.CreatedEvent;
import de.bennyboer.author.user.login.LoggedInEvent;
import de.bennyboer.author.user.login.LoginFailedEvent;
import de.bennyboer.author.user.remove.RemovedEvent;
import de.bennyboer.author.user.rename.RenamedEvent;
import de.bennyboer.author.user.snapshot.SnapshottedEvent;

import java.time.Instant;
import java.util.Map;

public class UserEventTransformer {

    public static Map<String, Object> toApi(Event event) {
        return switch (event) {
            case CreatedEvent createdEvent -> Map.of(
                    "name", createdEvent.getName().getValue()
            );
            case RenamedEvent renamedEvent -> Map.of(
                    "newName", renamedEvent.getNewName().getValue()
            );
            default -> Map.of();
        };
    }

    public static Map<String, Object> toSerialized(Event event) {
        return switch (event) {
            case CreatedEvent createdEvent -> Map.of(
                    "name", createdEvent.getName().getValue(),
                    "password", createdEvent.getPassword().getValue()
            );
            case RenamedEvent renamedEvent -> Map.of(
                    "newName", renamedEvent.getNewName().getValue()
            );
            case SnapshottedEvent snapshottedEvent -> Map.of(
                    "name", snapshottedEvent.getName().getValue(),
                    "password", snapshottedEvent.getPassword().getValue(),
                    "createdAt", snapshottedEvent.getCreatedAt().toString()
            );
            case LoggedInEvent ignoredEvent -> Map.of();
            case LoginFailedEvent ignoredEvent -> Map.of();
            case RemovedEvent ignoredEvent -> Map.of();
            default -> throw new IllegalArgumentException("Unknown event: " + event.getEventName());
        };
    }

    public static Event fromSerialized(Map<String, Object> payload, EventName eventName, Version ignoredVersion) {
        UserEvent event = UserEvent.fromName(eventName);

        return switch (event) {
            case CREATED -> CreatedEvent.ofStored(
                    UserName.of(payload.get("name").toString()),
                    Password.of(payload.get("password").toString())
            );
            case LOGGED_IN -> LoggedInEvent.of();
            case LOGIN_FAILED -> LoginFailedEvent.of();
            case REMOVED -> RemovedEvent.of();
            case SNAPSHOTTED -> SnapshottedEvent.of(
                    UserName.of(payload.get("name").toString()),
                    Password.of(payload.get("password").toString()),
                    Instant.parse(payload.get("createdAt").toString())
            );
            case RENAMED -> RenamedEvent.of(
                    UserName.of(payload.get("newName").toString())
            );
        };
    }

}
