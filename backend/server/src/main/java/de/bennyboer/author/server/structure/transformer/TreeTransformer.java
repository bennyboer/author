package de.bennyboer.author.server.structure.transformer;

import de.bennyboer.author.server.structure.api.TreeDTO;
import de.bennyboer.author.structure.tree.Tree;

public class TreeTransformer {

    public static TreeDTO toApi(Tree tree) {
        return TreeDTO.builder()
                .id(tree.getId().getValue())
                .version(tree.getVersion())
                .rootNodeId(tree.getRootNodeId().getValue())
                .nodes(NodeTransformer.toApi(tree.getNodes()))
                .build();
    }

}
