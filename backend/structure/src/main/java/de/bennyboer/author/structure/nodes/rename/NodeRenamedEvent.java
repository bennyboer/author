package de.bennyboer.author.structure.nodes.rename;

import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.Event;
import de.bennyboer.author.eventsourcing.event.EventName;
import de.bennyboer.author.structure.nodes.NodeId;
import de.bennyboer.author.structure.nodes.NodeName;
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
