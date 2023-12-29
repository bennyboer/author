package de.bennyboer.author.structure.nodes.toggle;

import de.bennyboer.author.eventsourcing.command.Command;
import de.bennyboer.author.structure.nodes.NodeId;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ToggleNodeCmd implements Command {

    NodeId nodeId;

    public static ToggleNodeCmd of(NodeId nodeId) {
        checkNotNull(nodeId, "NodeId to toggle must be given");

        return new ToggleNodeCmd(nodeId);
    }

}
