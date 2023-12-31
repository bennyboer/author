package de.bennyboer.author.structure.nodes.add;

import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.Event;
import de.bennyboer.author.eventsourcing.event.EventName;
import de.bennyboer.author.structure.StructureEvent;
import de.bennyboer.author.structure.nodes.NodeId;
import de.bennyboer.author.structure.nodes.NodeName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NodeAddedEvent implements Event {

    private static final Version VERSION = Version.zero();

    NodeId parentNodeId;

    NodeId newNodeId;

    NodeName newNodeName;

    public static NodeAddedEvent of(NodeId parentNodeId, NodeId newNodeId, NodeName newNodeName) {
        checkNotNull(parentNodeId, "Parent node ID must be given");
        checkNotNull(newNodeId, "New node ID must be given");
        checkNotNull(newNodeName, "New node name must be given");

        return new NodeAddedEvent(parentNodeId, newNodeId, newNodeName);
    }

    @Override
    public EventName getEventName() {
        return StructureEvent.NODE_ADDED.getName();
    }

    @Override
    public Version getVersion() {
        return VERSION;
    }

}
