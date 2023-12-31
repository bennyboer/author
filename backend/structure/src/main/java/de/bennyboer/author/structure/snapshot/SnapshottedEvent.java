package de.bennyboer.author.structure.snapshot;

import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.Event;
import de.bennyboer.author.eventsourcing.event.EventName;
import de.bennyboer.author.eventsourcing.event.SnapshotEvent;
import de.bennyboer.author.structure.StructureEvent;
import de.bennyboer.author.structure.nodes.Node;
import de.bennyboer.author.structure.nodes.NodeId;
import jakarta.annotation.Nullable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SnapshottedEvent implements Event, SnapshotEvent {

    private static final Version VERSION = Version.zero();

    String projectId;

    NodeId rootNodeId;

    Map<NodeId, Node> nodes;

    Instant createdAt;

    @Nullable
    Instant removedAt;

    public static SnapshottedEvent of(
            String projectId,
            NodeId rootNodeId,
            Map<NodeId, Node> nodes,
            Instant createdAt,
            @Nullable Instant removedAt
    ) {
        checkNotNull(projectId, "Project ID must be given");
        checkNotNull(rootNodeId, "Root node ID must be given");
        checkNotNull(nodes, "Nodes must be given");
        checkNotNull(createdAt, "Created at must be given");

        return new SnapshottedEvent(
                projectId,
                rootNodeId,
                nodes,
                createdAt,
                removedAt
        );
    }

    @Override
    public EventName getEventName() {
        return StructureEvent.SNAPSHOTTED.getName();
    }

    @Override
    public Version getVersion() {
        return VERSION;
    }

    public Optional<Instant> getRemovedAt() {
        return Optional.ofNullable(removedAt);
    }

}
