package de.bennyboer.author.structure.create;

import de.bennyboer.author.eventsourcing.command.Command;
import de.bennyboer.author.structure.nodes.Node;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateCmd implements Command {

    String projectId;

    Node rootNode;

    public static CreateCmd of(String projectId, Node rootNode) {
        checkNotNull(projectId, "Project ID must be given");
        checkNotNull(rootNode, "Root node must be given");

        return new CreateCmd(projectId, rootNode);
    }

}
