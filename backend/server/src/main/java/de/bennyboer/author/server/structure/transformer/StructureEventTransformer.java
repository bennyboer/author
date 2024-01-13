package de.bennyboer.author.server.structure.transformer;

import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.Event;
import de.bennyboer.author.eventsourcing.event.EventName;
import de.bennyboer.author.structure.StructureEvent;
import de.bennyboer.author.structure.create.CreatedEvent;
import de.bennyboer.author.structure.nodes.Node;
import de.bennyboer.author.structure.nodes.NodeId;
import de.bennyboer.author.structure.nodes.NodeName;
import de.bennyboer.author.structure.nodes.add.NodeAddedEvent;
import de.bennyboer.author.structure.nodes.remove.NodeRemovedEvent;
import de.bennyboer.author.structure.nodes.rename.NodeRenamedEvent;
import de.bennyboer.author.structure.nodes.swap.NodesSwappedEvent;
import de.bennyboer.author.structure.nodes.toggle.NodeToggledEvent;
import de.bennyboer.author.structure.remove.RemovedEvent;
import de.bennyboer.author.structure.snapshot.SnapshottedEvent;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StructureEventTransformer {

    public static Map<String, Object> toApi(Event event) {
        return switch (event) {
            case NodeAddedEvent nodeAddedEvent -> Map.of(
                    "parentNodeId", nodeAddedEvent.getParentNodeId().getValue(),
                    "newNodeId", nodeAddedEvent.getNewNodeId().getValue(),
                    "newNodeName", nodeAddedEvent.getNewNodeName().getValue()
            );
            case NodeRemovedEvent nodeRemovedEvent -> Map.of(
                    "nodeId", nodeRemovedEvent.getNodeId().getValue()
            );
            case NodeToggledEvent nodeToggledEvent -> Map.of(
                    "nodeId", nodeToggledEvent.getNodeId().getValue()
            );
            case NodesSwappedEvent nodesSwappedEvent -> Map.of(
                    "nodeId1", nodesSwappedEvent.getNodeId1().getValue(),
                    "nodeId2", nodesSwappedEvent.getNodeId2().getValue()
            );
            case NodeRenamedEvent nodeRenamedEvent -> Map.of(
                    "nodeId", nodeRenamedEvent.getNodeId().getValue(),
                    "newNodeName", nodeRenamedEvent.getNewNodeName().getValue()
            );
            default -> Map.of();
        };
    }

    public static Map<String, Object> toSerialized(Event event) {
        return switch (event) {
            case CreatedEvent createdEvent -> Map.of(
                    "projectId", createdEvent.getProjectId(),
                    "rootNodeId", createdEvent.getRootNode().getId().getValue(),
                    "rootNodeName", createdEvent.getRootNode().getName().getValue()
            );
            case RemovedEvent ignoredEvent -> Map.of();
            case SnapshottedEvent snapshottedEvent -> Map.of(
                    "projectId", snapshottedEvent.getProjectId(),
                    "rootNodeId", snapshottedEvent.getRootNodeId().getValue(),
                    "nodes", toSerializedNodes(snapshottedEvent.getNodes()),
                    "createdAt", snapshottedEvent.getCreatedAt().toString(),
                    "removedAt", snapshottedEvent.getRemovedAt()
            );
            case NodeAddedEvent nodeAddedEvent -> Map.of(
                    "parentNodeId", nodeAddedEvent.getParentNodeId().getValue(),
                    "newNodeId", nodeAddedEvent.getNewNodeId().getValue(),
                    "newNodeName", nodeAddedEvent.getNewNodeName().getValue()
            );
            case NodeRemovedEvent nodeRemovedEvent -> Map.of(
                    "nodeId", nodeRemovedEvent.getNodeId().getValue()
            );
            case NodeRenamedEvent nodeRenamedEvent -> Map.of(
                    "nodeId", nodeRenamedEvent.getNodeId().getValue(),
                    "newNodeName", nodeRenamedEvent.getNewNodeName().getValue()
            );
            case NodesSwappedEvent nodesSwappedEvent -> Map.of(
                    "nodeId1", nodesSwappedEvent.getNodeId1().getValue(),
                    "nodeId2", nodesSwappedEvent.getNodeId2().getValue()
            );
            case NodeToggledEvent nodeToggledEvent -> Map.of(
                    "nodeId", nodeToggledEvent.getNodeId().getValue()
            );
            default -> throw new IllegalStateException("Unexpected event: " + event.getEventName());
        };
    }

    public static Event fromSerialized(Map<String, Object> payload, EventName eventName, Version ignoredVersion) {
        StructureEvent event = StructureEvent.fromName(eventName);

        return switch (event) {
            case CREATED -> CreatedEvent.of(
                    payload.get("projectId").toString(),
                    Node.of(
                            NodeId.of(payload.get("rootNodeId").toString()),
                            NodeName.of(payload.get("rootNodeName").toString()),
                            null,
                            List.of(),
                            true
                    )
            );
            case REMOVED -> RemovedEvent.of();
            case SNAPSHOTTED -> SnapshottedEvent.of(
                    payload.get("projectId").toString(),
                    NodeId.of(payload.get("rootNodeId").toString()),
                    fromSerializedNodes((Map<String, Object>) payload.get("nodes")),
                    Instant.parse(payload.get("createdAt").toString()),
                    Optional.ofNullable(payload.get("removedAt"))
                            .map(Object::toString)
                            .map(Instant::parse)
                            .orElse(null)
            );
            case NODE_ADDED -> NodeAddedEvent.of(
                    NodeId.of(payload.get("parentNodeId").toString()),
                    NodeId.of(payload.get("newNodeId").toString()),
                    NodeName.of(payload.get("newNodeName").toString())
            );
            case NODE_REMOVED -> NodeRemovedEvent.of(
                    NodeId.of(payload.get("nodeId").toString())
            );
            case NODE_RENAMED -> NodeRenamedEvent.of(
                    NodeId.of(payload.get("nodeId").toString()),
                    NodeName.of(payload.get("newNodeName").toString())
            );
            case NODES_SWAPPED -> NodesSwappedEvent.of(
                    NodeId.of(payload.get("nodeId1").toString()),
                    NodeId.of(payload.get("nodeId2").toString())
            );
            case NODE_TOGGLED -> NodeToggledEvent.of(
                    NodeId.of(payload.get("nodeId").toString())
            );
        };
    }

    private static Map<String, Object> toSerializedNodes(Map<NodeId, Node> nodes) {
        Map<String, Object> result = new HashMap<>();

        nodes.forEach((nodeId, node) -> result.put(nodeId.getValue(), toSerializedNode(node)));

        return result;
    }

    private static Map<NodeId, Node> fromSerializedNodes(Map<String, Object> serialized) {
        Map<NodeId, Node> result = new HashMap<>();

        serialized.forEach((nodeId, node) -> result.put(
                NodeId.of(nodeId),
                fromSerializedNode((Map<String, Object>) node)
        ));

        return result;
    }

    private static Map<String, Object> toSerializedNode(Node node) {
        String id = node.getId().getValue();
        Optional<String> parentId = node.getParentId().map(NodeId::getValue);
        String name = node.getName().getValue();
        boolean expanded = node.isExpanded();
        List<String> children = node.getChildren()
                .stream()
                .map(NodeId::getValue)
                .toList();

        return Map.of(
                "id", id,
                "parentId", parentId,
                "name", name,
                "expanded", expanded,
                "children", children
        );
    }

    private static Node fromSerializedNode(Map<String, Object> serialized) {
        NodeId id = NodeId.of(serialized.get("id").toString());
        NodeId parentId = Optional.ofNullable(serialized.get("parentId"))
                .map(Object::toString)
                .map(NodeId::of)
                .orElse(null);
        NodeName name = NodeName.of(serialized.get("name").toString());
        boolean expanded = Boolean.parseBoolean(serialized.get("expanded").toString());
        List<String> children = (List<String>) serialized.get("children");
        List<NodeId> childrenIds = children.stream()
                .map(NodeId::of)
                .toList();

        return Node.of(id, name, parentId, childrenIds, expanded);
    }

}
