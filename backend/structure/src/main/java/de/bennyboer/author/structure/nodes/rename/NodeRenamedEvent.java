package de.bennyboer.author.structure.nodes.rename;

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
public class NodeRenamedEvent implements Event {

    private static final Version VERSION = Version.zero();

    NodeId nodeId;

    NodeName newNodeName;

    public static NodeRenamedEvent of(NodeId nodeId, NodeName newNodeName) {
        checkNotNull(nodeId, "Node ID must be given");
        checkNotNull(newNodeName, "New node name must be given");

        return new NodeRenamedEvent(nodeId, newNodeName);
    }

    @Override
    public EventName getEventName() {
        return StructureEvent.NODE_RENAMED.getName();
    }

    @Override
    public Version getVersion() {
        return VERSION;
    }

}
