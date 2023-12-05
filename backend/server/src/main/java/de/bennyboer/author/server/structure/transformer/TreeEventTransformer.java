package de.bennyboer.author.server.structure.transformer;

import de.bennyboer.author.structure.tree.events.*;
import de.bennyboer.eventsourcing.api.event.Event;

import java.util.Map;

public class TreeEventTransformer {

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

}
