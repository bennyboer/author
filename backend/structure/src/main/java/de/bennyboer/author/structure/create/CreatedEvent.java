package de.bennyboer.author.structure.create;

import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.Event;
import de.bennyboer.author.eventsourcing.event.EventName;
import de.bennyboer.author.structure.StructureEvent;
import de.bennyboer.author.structure.nodes.Node;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CreatedEvent implements Event {

    private static final Version VERSION = Version.zero();

    String projectId;

    Node rootNode;

    public static CreatedEvent of(String projectId, Node rootNode) {
        checkNotNull(projectId, "Project ID must be given");
        checkNotNull(rootNode, "Root node must be given");
        
        return new CreatedEvent(projectId, rootNode);
    }

    @Override
    public EventName getEventName() {
        return StructureEvent.CREATED.getName();
    }

    @Override
    public Version getVersion() {
        return VERSION;
    }

}
