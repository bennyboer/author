package de.bennyboer.author.server.project.transformer;

import de.bennyboer.author.project.create.CreatedEvent;
import de.bennyboer.author.project.rename.RenamedEvent;
import de.bennyboer.eventsourcing.event.Event;

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

}
