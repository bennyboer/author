package de.bennyboer.author.structure.tree.snapshot;

import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.Event;
import de.bennyboer.author.eventsourcing.event.EventName;
import de.bennyboer.author.structure.tree.Tree;
import de.bennyboer.author.structure.tree.nodes.Node;
import de.bennyboer.author.structure.tree.nodes.NodeId;
import jakarta.annotation.Nullable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SnapshottedEvent implements Event {

    public static final EventName NAME = EventName.of("SNAPSHOTTED");

    public static final Version VERSION = Version.zero();

    String projectId;

    NodeId rootNodeId;

    Map<NodeId, Node> nodes;

    Instant createdAt;

    @Nullable
    Instant removedAt;

    public static SnapshottedEvent of(Tree tree) {
        return new SnapshottedEvent(
                tree.getProjectId(),
                tree.getRootNodeId(),
                tree.getNodes(),
                tree.getCreatedAt(),
                tree.getRemovedAt().orElse(null)
        );
    }

    @Override
    public EventName getEventName() {
        return NAME;
    }

    @Override
    public Version getVersion() {
        return VERSION;
    }

    public Optional<Instant> getRemovedAt() {
        return Optional.ofNullable(removedAt);
    }

}
