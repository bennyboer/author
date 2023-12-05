package de.bennyboer.author.structure.tree.commands;

import de.bennyboer.author.structure.tree.api.NodeId;
import de.bennyboer.author.structure.tree.api.NodeName;
import de.bennyboer.eventsourcing.api.command.Command;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RenameNodeCmd implements Command {

    NodeId nodeId;

    NodeName newNodeName;

    public static RenameNodeCmd of(NodeId nodeId, NodeName newNodeName) {
        checkNotNull(nodeId, "nodeId must not be null");
        checkNotNull(newNodeName, "newNodeName must not be null");

        return new RenameNodeCmd(nodeId, newNodeName);
    }

}
