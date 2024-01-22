package de.bennyboer.author.server.projects.transformer;

import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.Event;
import de.bennyboer.author.eventsourcing.event.EventName;
import de.bennyboer.author.project.ProjectEvent;
import de.bennyboer.author.project.ProjectName;
import de.bennyboer.author.project.create.CreatedEvent;
import de.bennyboer.author.project.remove.RemovedEvent;
import de.bennyboer.author.project.rename.RenamedEvent;
import de.bennyboer.author.project.snapshot.SnapshottedEvent;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ProjectEventTransformer {

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
                    "name", createdEvent.getName().getValue()
            );
            case RemovedEvent ignoredEvent -> Map.of();
            case RenamedEvent renamedEvent -> Map.of(
                    "newName", renamedEvent.getNewName().getValue()
            );
            case SnapshottedEvent snapshottedEvent -> {
                var result = new HashMap<String, Object>(Map.of(
                        "name", snapshottedEvent.getName().getValue(),
                        "createdAt", snapshottedEvent.getCreatedAt()
                ));

                snapshottedEvent.getRemovedAt().ifPresent(removedAt -> result.put("removedAt", removedAt));

                yield result;
            }
            default -> throw new IllegalArgumentException("Unknown event type: " + event.getClass().getName());
        };
    }

    public static Event fromSerialized(Map<String, Object> serialized, EventName eventName, Version ignoredVersion) {
        ProjectEvent event = ProjectEvent.fromName(eventName);

        return switch (event) {
            case CREATED -> CreatedEvent.of(ProjectName.of(serialized.get("name").toString()));
            case REMOVED -> RemovedEvent.of();
            case RENAMED -> RenamedEvent.of(ProjectName.of(serialized.get("newName").toString()));
            case SNAPSHOTTED -> {
                ProjectName name = ProjectName.of(serialized.get("name").toString());
                Instant createdAt = Instant.parse(serialized.get("createdAt").toString());
                Instant removedAt = Optional.ofNullable(serialized.get("removedAt"))
                        .map(Object::toString)
                        .map(Instant::parse)
                        .orElse(null);

                yield SnapshottedEvent.of(name, createdAt, removedAt);
            }
        };
    }

}
