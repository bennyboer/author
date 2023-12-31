package de.bennyboer.author.structure.nodes.toggle;

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
public class NodeToggledEvent implements Event {

    private static final Version VERSION = Version.zero();

    NodeId nodeId;

    public static NodeToggledEvent of(NodeId nodeId) {
        checkNotNull(nodeId, "Node ID must be given");

        return new NodeToggledEvent(nodeId);
    }

    @Override
    public EventName getEventName() {
        return StructureEvent.NODE_TOGGLED.getName();
    }

    @Override
    public Version getVersion() {
        return VERSION;
    }

}
