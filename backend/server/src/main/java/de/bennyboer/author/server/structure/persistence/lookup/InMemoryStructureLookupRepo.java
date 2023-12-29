package de.bennyboer.author.server.structure.persistence.lookup;

import de.bennyboer.author.structure.Structure;
import de.bennyboer.author.structure.StructureId;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryStructureLookupRepo implements StructureLookupRepo {

    private final Map<String, StructureId> projectIdToStructureId = new ConcurrentHashMap<>();

    @Override
    public Mono<StructureId> findStructureIdByProjectId(String projectId) {
        return Mono.justOrEmpty(projectIdToStructureId.get(projectId));
    }

    @Override
    public Mono<Void> update(Structure structure) {
        return Mono.fromRunnable(() -> projectIdToStructureId.put(structure.getProjectId(), structure.getId()));
    }

    @Override
    public Mono<Void> remove(StructureId structureId) {
        return Mono.fromRunnable(() -> {
            for (Map.Entry<String, StructureId> entry : projectIdToStructureId.entrySet()) {
                if (entry.getValue().equals(structureId)) {
                    projectIdToStructureId.remove(entry.getKey());
                }
            }
        });
    }

}
