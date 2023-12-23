package de.bennyboer.author.structure.tree.nodes.swap;

import de.bennyboer.author.structure.tree.nodes.NodeId;
import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.Event;
import de.bennyboer.author.eventsourcing.event.EventName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NodesSwappedEvent implements Event {

    public static final EventName NAME = EventName.of("NODES_SWAPPED");

    public static final Version VERSION = Version.zero();

    NodeId nodeId1;

    NodeId nodeId2;

    public static NodesSwappedEvent of(SwapNodesCmd cmd) {
        return new NodesSwappedEvent(cmd.getNodeId1(), cmd.getNodeId2());
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
