package de.bennyboer.author.server.structure.persistence.lookup;

import de.bennyboer.author.eventsourcing.persistence.readmodel.InMemoryEventSourcingReadModelRepo;
import de.bennyboer.author.structure.StructureId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

public class InMemoryStructureLookupRepo extends InMemoryEventSourcingReadModelRepo<StructureId, LookupStructure>
        implements StructureLookupRepo {

    @Override
    public Mono<StructureId> findStructureIdByProjectId(String projectId) {
        return Flux.fromIterable(lookup.entrySet())
                .filter(entry -> entry.getValue().getProjectId().equals(projectId))
                .map(Map.Entry::getKey)
                .next();
    }

    @Override
    protected StructureId getId(LookupStructure aggregate) {
        return aggregate.getId();
    }

}
