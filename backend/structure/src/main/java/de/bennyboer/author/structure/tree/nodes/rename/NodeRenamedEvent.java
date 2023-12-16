package de.bennyboer.author.structure.tree.nodes.rename;

import de.bennyboer.author.structure.tree.nodes.NodeId;
import de.bennyboer.author.structure.tree.nodes.NodeName;
import de.bennyboer.eventsourcing.Version;
import de.bennyboer.eventsourcing.event.Event;
import de.bennyboer.eventsourcing.event.EventName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NodeRenamedEvent implements Event {

    public static final EventName NAME = EventName.of("NODE_RENAMED");

    public static final Version VERSION = Version.zero();

    NodeId nodeId;

    NodeName newNodeName;

    public static NodeRenamedEvent of(RenameNodeCmd cmd) {
        return new NodeRenamedEvent(cmd.getNodeId(), cmd.getNewNodeName());
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
