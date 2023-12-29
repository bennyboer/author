package de.bennyboer.author.structure.nodes.remove;

import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.Event;
import de.bennyboer.author.eventsourcing.event.EventName;
import de.bennyboer.author.structure.nodes.NodeId;
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
