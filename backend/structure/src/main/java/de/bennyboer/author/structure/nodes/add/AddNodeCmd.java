package de.bennyboer.author.structure.nodes.add;

import de.bennyboer.author.eventsourcing.command.Command;
import de.bennyboer.author.structure.nodes.NodeId;
import de.bennyboer.author.structure.nodes.NodeName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AddNodeCmd implements Command {

    NodeId parentNodeId;

    NodeId newNodeId;

    NodeName newNodeName;

    public static AddNodeCmd of(NodeId parentNodeId, NodeId newNodeId, NodeName newNodeName) {
        checkNotNull(parentNodeId, "Parent NodeId must be given");
        checkNotNull(newNodeId, "New NodeId must be given");
        checkNotNull(newNodeName, "New NodeName must be given");

        return new AddNodeCmd(parentNodeId, newNodeId, newNodeName);
    }

}
