package de.bennyboer.author.structure.tree.nodes.add;

import de.bennyboer.author.structure.tree.nodes.NodeId;
import de.bennyboer.author.structure.tree.nodes.NodeName;
import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.Event;
import de.bennyboer.author.eventsourcing.event.EventName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NodeAddedEvent implements Event {

    public static final EventName NAME = EventName.of("NODE_ADDED");

    public static final Version VERSION = Version.zero();

    NodeId parentNodeId;

    NodeId newNodeId;

    NodeName newNodeName;

    public static NodeAddedEvent of(AddNodeCmd cmd) {
        return new NodeAddedEvent(cmd.getParentNodeId(), cmd.getNewNodeId(), cmd.getNewNodeName());
    }

    @Override
    public EventName getEventName() {
        return NAME;
    }

    @Override
    public Version getVersion() {
        return VERSION;
    }

}
