package de.bennyboer.author.structure.tree.events;

import de.bennyboer.author.structure.tree.model.Node;
import de.bennyboer.author.structure.tree.model.NodeId;
import de.bennyboer.author.structure.tree.model.Tree;
import de.bennyboer.eventsourcing.api.Version;
import de.bennyboer.eventsourcing.api.event.Event;
import de.bennyboer.eventsourcing.api.event.EventName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Map;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SnapshottedEvent implements Event {

    public static final EventName NAME = EventName.of("SNAPSHOTTED");

    public static final Version VERSION = Version.zero();

    NodeId rootNodeId;

    Map<NodeId, Node> nodes;

    public static SnapshottedEvent of(Tree tree) {
        return new SnapshottedEvent(tree.getRootNodeId(), tree.getNodes());
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
