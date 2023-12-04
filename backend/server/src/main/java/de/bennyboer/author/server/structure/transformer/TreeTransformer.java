package de.bennyboer.author.server.structure.transformer;

import de.bennyboer.author.server.structure.api.TreeDTO;
import de.bennyboer.author.structure.tree.api.Tree;

public class TreeTransformer {

    public static TreeDTO toApi(Tree tree) {
        return TreeDTO.builder()
                .version(tree.getVersion())
                .rootNodeId(tree.getRootNodeId().getValue())
                .nodes(NodeTransformer.toApi(tree.getNodes()))
                .build();
    }

}
