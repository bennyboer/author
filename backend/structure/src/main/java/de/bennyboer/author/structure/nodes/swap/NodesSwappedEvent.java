package de.bennyboer.author.structure.nodes.swap;

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
public class NodesSwappedEvent implements Event {

    private static final Version VERSION = Version.zero();

    NodeId nodeId1;

    NodeId nodeId2;

    public static NodesSwappedEvent of(NodeId nodeId1, NodeId nodeId2) {
        checkNotNull(nodeId1, "Node ID 1 must be given");
        checkNotNull(nodeId2, "Node ID 2 must be given");

        return new NodesSwappedEvent(nodeId1, nodeId2);
    }

    @Override
    public EventName getEventName() {
        return StructureEvent.NODES_SWAPPED.getName();
    }

    @Override
    public Version getVersion() {
        return VERSION;
    }

}
