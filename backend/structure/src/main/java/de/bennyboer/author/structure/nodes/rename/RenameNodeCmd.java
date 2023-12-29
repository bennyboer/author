package de.bennyboer.author.structure.nodes.rename;

import de.bennyboer.author.eventsourcing.command.Command;
import de.bennyboer.author.structure.nodes.NodeId;
import de.bennyboer.author.structure.nodes.NodeName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RenameNodeCmd implements Command {

    NodeId nodeId;

    NodeName newNodeName;

    public static RenameNodeCmd of(NodeId nodeId, NodeName newNodeName) {
        checkNotNull(nodeId, "nodeId must be given");
        checkNotNull(newNodeName, "newNodeName must be given");

        return new RenameNodeCmd(nodeId, newNodeName);
    }

}
