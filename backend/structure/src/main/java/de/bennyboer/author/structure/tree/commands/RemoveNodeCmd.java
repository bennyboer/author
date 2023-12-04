package de.bennyboer.author.structure.tree.commands;

import de.bennyboer.author.structure.tree.api.NodeId;
import de.bennyboer.eventsourcing.api.command.Command;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RemoveNodeCmd implements Command {

    NodeId nodeId;

    public static RemoveNodeCmd of(NodeId nodeId) {
        if (nodeId == null) {
            throw new IllegalArgumentException("NodeId to remove must not be null");
        }

        return new RemoveNodeCmd(nodeId);
    }

}
