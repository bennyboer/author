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
import java.util.Map;

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
            case SnapshottedEvent snapshottedEvent -> Map.of(
                    "name", snapshottedEvent.getName().getValue(),
                    "createdAt", snapshottedEvent.getCreatedAt()
            );
            default -> throw new IllegalArgumentException("Unknown event type: " + event.getClass().getName());
        };
    }

    public static Event fromSerialized(Map<String, Object> serialized, EventName eventName, Version ignoredVersion) {
        ProjectEvent event = ProjectEvent.fromName(eventName);

        return switch (event) {
            case CREATED -> CreatedEvent.of(ProjectName.of(serialized.get("name").toString()));
            case REMOVED -> RemovedEvent.of();
            case RENAMED -> RenamedEvent.of(ProjectName.of(serialized.get("newName").toString()));
            case SNAPSHOTTED -> SnapshottedEvent.of(
                    ProjectName.of(serialized.get("name").toString()),
                    Instant.parse(serialized.get("createdAt").toString())
            );
        };
    }

}
