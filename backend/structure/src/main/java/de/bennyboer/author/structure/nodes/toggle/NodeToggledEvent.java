package de.bennyboer.author.structure.nodes.toggle;

import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.Event;
import de.bennyboer.author.eventsourcing.event.EventName;
import de.bennyboer.author.structure.nodes.NodeId;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NodeToggledEvent implements Event {

    public static final EventName NAME = EventName.of("NODE_TOGGLED");

    public static final Version VERSION = Version.zero();

    NodeId nodeId;

    public static NodeToggledEvent of(ToggleNodeCmd cmd) {
        return new NodeToggledEvent(cmd.getNodeId());
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
