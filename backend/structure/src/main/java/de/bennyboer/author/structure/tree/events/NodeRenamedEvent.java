package de.bennyboer.author.structure.tree.events;

import de.bennyboer.author.structure.tree.api.NodeId;
import de.bennyboer.author.structure.tree.api.NodeName;
import de.bennyboer.author.structure.tree.commands.RenameNodeCmd;
import de.bennyboer.eventsourcing.api.Version;
import de.bennyboer.eventsourcing.api.event.Event;
import de.bennyboer.eventsourcing.api.event.EventName;
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
    public EventName getName() {
        return NAME;
    }

    @Override
    public Version getVersion() {
        return VERSION;
    }

}
