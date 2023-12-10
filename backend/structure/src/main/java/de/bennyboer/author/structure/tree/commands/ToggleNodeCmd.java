package de.bennyboer.author.structure.tree.commands;

import de.bennyboer.author.structure.tree.node.NodeId;
import de.bennyboer.eventsourcing.command.Command;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ToggleNodeCmd implements Command {

    NodeId nodeId;

    public static ToggleNodeCmd of(NodeId nodeId) {
        checkNotNull(nodeId, "NodeId to toggle must not be null");

        return new ToggleNodeCmd(nodeId);
    }

}
