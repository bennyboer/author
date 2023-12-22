package de.bennyboer.author.structure.tree.nodes.remove;

import de.bennyboer.author.structure.tree.nodes.NodeId;
import de.bennyboer.eventsourcing.Version;
import de.bennyboer.eventsourcing.event.Event;
import de.bennyboer.eventsourcing.event.EventName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NodeRemovedEvent implements Event {

    public static final EventName NAME = EventName.of("NODE_REMOVED");

    public static final Version VERSION = Version.zero();

    NodeId nodeId;

    public static NodeRemovedEvent of(RemoveNodeCmd cmd) {
        return new NodeRemovedEvent(cmd.getNodeId());
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