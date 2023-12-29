package de.bennyboer.author.structure.nodes.remove;

import de.bennyboer.author.eventsourcing.command.Command;
import de.bennyboer.author.structure.nodes.NodeId;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RemoveNodeCmd implements Command {

    NodeId nodeId;

    public static RemoveNodeCmd of(NodeId nodeId) {
        checkNotNull(nodeId, "NodeId must be given");

        return new RemoveNodeCmd(nodeId);
    }

}
