package de.bennyboer.author.structure.tree.events;

import de.bennyboer.author.structure.tree.commands.ToggleNodeCmd;
import de.bennyboer.author.structure.tree.model.NodeId;
import de.bennyboer.eventsourcing.api.Version;
import de.bennyboer.eventsourcing.api.event.Event;
import de.bennyboer.eventsourcing.api.event.EventName;
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
    public EventName getName() {
        return NAME;
    }

    @Override
    public Version getVersion() {
        return VERSION;
    }

}
