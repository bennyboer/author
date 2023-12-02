package de.bennyboer.author.structure.tree.commands;

import de.bennyboer.author.structure.tree.node.NodeId;
import de.bennyboer.author.structure.tree.node.NodeName;
import de.bennyboer.eventsourcing.api.command.Command;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AddNodeCmd implements Command {

    NodeId parentNodeId;

    NodeId newNodeId;

    NodeName newNodeName;

    public static AddNodeCmd of(NodeId parentNodeId, NodeId newNodeId, NodeName newNodeName) {
        if (parentNodeId == null) {
            throw new IllegalArgumentException("Parent NodeId must not be null");
        }
        if (newNodeId == null) {
            throw new IllegalArgumentException("New NodeId must not be null");
        }
        if (newNodeName == null) {
            throw new IllegalArgumentException("New NodeName must not be null");
        }

        return new AddNodeCmd(parentNodeId, newNodeId, newNodeName);
    }

}
