package de.bennyboer.author.server.structure.persistence.lookup;

import de.bennyboer.author.structure.Structure;
import de.bennyboer.author.structure.StructureId;
import reactor.core.publisher.Mono;

public interface StructureLookupRepo {

    Mono<StructureId> findStructureIdByProjectId(String projectId);

    Mono<Void> update(Structure structure);

    Mono<Void> remove(StructureId structureId);

}
