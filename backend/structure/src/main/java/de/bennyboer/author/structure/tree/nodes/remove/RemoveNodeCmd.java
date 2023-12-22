package de.bennyboer.author.structure.tree.nodes.remove;

import de.bennyboer.author.structure.tree.nodes.NodeId;
import de.bennyboer.eventsourcing.command.Command;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RemoveNodeCmd implements Command {

    NodeId nodeId;

    public static RemoveNodeCmd of(NodeId nodeId) {
        checkNotNull(nodeId, "NodeId must not be null");

        return new RemoveNodeCmd(nodeId);
    }

}
