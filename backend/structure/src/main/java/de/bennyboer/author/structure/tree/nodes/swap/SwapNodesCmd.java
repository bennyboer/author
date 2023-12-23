package de.bennyboer.author.structure.tree.nodes.swap;

import de.bennyboer.author.structure.tree.nodes.NodeId;
import de.bennyboer.author.eventsourcing.command.Command;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SwapNodesCmd implements Command {

    NodeId nodeId1;

    NodeId nodeId2;

    public static SwapNodesCmd of(NodeId nodeId1, NodeId nodeId2) {
        checkNotNull(nodeId1, "NodeId 1 to swap must not be null");
        checkNotNull(nodeId2, "NodeId 2 to swap with must not be null");

        return new SwapNodesCmd(nodeId1, nodeId2);
    }

}
