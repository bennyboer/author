package de.bennyboer.author.structure.nodes.swap;

import de.bennyboer.author.eventsourcing.command.Command;
import de.bennyboer.author.structure.nodes.NodeId;
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
        checkNotNull(nodeId1, "NodeId 1 to swap must be given");
        checkNotNull(nodeId2, "NodeId 2 to swap with must be given");

        return new SwapNodesCmd(nodeId1, nodeId2);
    }

}
