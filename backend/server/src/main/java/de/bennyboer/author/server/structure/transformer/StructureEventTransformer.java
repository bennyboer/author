package de.bennyboer.author.server.structure.transformer;

import de.bennyboer.author.eventsourcing.event.Event;
import de.bennyboer.author.structure.nodes.add.NodeAddedEvent;
import de.bennyboer.author.structure.nodes.remove.NodeRemovedEvent;
import de.bennyboer.author.structure.nodes.rename.NodeRenamedEvent;
import de.bennyboer.author.structure.nodes.swap.NodesSwappedEvent;
import de.bennyboer.author.structure.nodes.toggle.NodeToggledEvent;

import java.util.Map;

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

}
