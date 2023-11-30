package de.bennyboer.author.structure.tree.commands;

import de.bennyboer.author.structure.tree.node.NodeId;
import de.bennyboer.eventsourcing.api.command.Command;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ToggleNodeCmd implements Command {

    NodeId nodeId;

    public static ToggleNodeCmd of(NodeId nodeId) {
        if (nodeId == null) {
            throw new IllegalArgumentException("NodeId to toggle must not be null");
        }

        return new ToggleNodeCmd(nodeId);
    }

}
