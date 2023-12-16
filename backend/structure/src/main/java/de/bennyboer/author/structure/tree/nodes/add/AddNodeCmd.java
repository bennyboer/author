package de.bennyboer.author.structure.tree.nodes.add;

import de.bennyboer.author.structure.tree.nodes.NodeId;
import de.bennyboer.author.structure.tree.nodes.NodeName;
import de.bennyboer.eventsourcing.command.Command;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AddNodeCmd implements Command {

    NodeId parentNodeId;

    NodeId newNodeId;

    NodeName newNodeName;

    public static AddNodeCmd of(NodeId parentNodeId, NodeId newNodeId, NodeName newNodeName) {
        checkNotNull(parentNodeId, "Parent NodeId must not be null");
        checkNotNull(newNodeId, "New NodeId must not be null");
        checkNotNull(newNodeName, "New NodeName must not be null");

        return new AddNodeCmd(parentNodeId, newNodeId, newNodeName);
    }

}
