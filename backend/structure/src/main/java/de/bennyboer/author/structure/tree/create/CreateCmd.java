package de.bennyboer.author.structure.tree.create;

import de.bennyboer.author.structure.tree.nodes.Node;
import de.bennyboer.eventsourcing.command.Command;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateCmd implements Command {

    Node rootNode;

    public static CreateCmd of(Node rootNode) {
        checkNotNull(rootNode, "Root node must not be null");

        return new CreateCmd(rootNode);
    }

}
