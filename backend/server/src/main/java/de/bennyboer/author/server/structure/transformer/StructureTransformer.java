package de.bennyboer.author.server.structure.transformer;

import de.bennyboer.author.server.structure.api.StructureDTO;
import de.bennyboer.author.structure.Structure;

public class StructureTransformer {

    public static StructureDTO toApi(Structure structure) {
        return StructureDTO.builder()
                .id(structure.getId().getValue())
                .version(structure.getVersion().getValue())
                .rootNodeId(structure.getRootNodeId().getValue())
                .nodes(NodeTransformer.toApi(structure.getNodes()))
                .build();
    }

}
