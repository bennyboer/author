package de.bennyboer.author.structure.tree.nodes.toggle;

import de.bennyboer.author.structure.tree.nodes.NodeId;
import de.bennyboer.author.eventsourcing.command.Command;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ToggleNodeCmd implements Command {

    NodeId nodeId;

    public static ToggleNodeCmd of(NodeId nodeId) {
        checkNotNull(nodeId, "NodeId to toggle must not be null");

        return new ToggleNodeCmd(nodeId);
    }

}
