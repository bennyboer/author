package de.bennyboer.author.server.structure.persistence.lookup;

import de.bennyboer.author.eventsourcing.persistence.readmodel.EventSourcingReadModelRepo;
import de.bennyboer.author.structure.StructureId;
import reactor.core.publisher.Mono;

public interface StructureLookupRepo extends EventSourcingReadModelRepo<StructureId, LookupStructure> {

    Mono<StructureId> findStructureIdByProjectId(String projectId);

}
