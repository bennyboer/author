package de.bennyboer.author.structure.nodes.remove;

import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.Event;
import de.bennyboer.author.eventsourcing.event.EventName;
import de.bennyboer.author.structure.StructureEvent;
import de.bennyboer.author.structure.nodes.NodeId;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NodeRemovedEvent implements Event {

    private static final Version VERSION = Version.zero();

    NodeId nodeId;

    public static NodeRemovedEvent of(NodeId nodeId) {
        checkNotNull(nodeId, "Node ID must be given");

        return new NodeRemovedEvent(nodeId);
    }

    @Override
    public EventName getEventName() {
        return StructureEvent.NODE_REMOVED.getName();
    }

    @Override
    public Version getVersion() {
        return VERSION;
    }

}
