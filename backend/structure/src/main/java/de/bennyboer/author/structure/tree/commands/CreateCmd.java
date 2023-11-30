package de.bennyboer.author.structure.tree.commands;

import de.bennyboer.author.structure.tree.node.Node;
import de.bennyboer.eventsourcing.api.command.Command;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateCmd implements Command {

    Node rootNode;

    public static CreateCmd of(Node rootNode) {
        if (rootNode == null) {
            throw new IllegalArgumentException("Root node must not be null");
        }

        return new CreateCmd(rootNode);
    }

}
