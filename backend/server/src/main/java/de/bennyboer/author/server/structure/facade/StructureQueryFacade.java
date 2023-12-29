package de.bennyboer.author.server.structure.facade;

import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.author.server.structure.api.StructureDTO;
import de.bennyboer.author.server.structure.permissions.StructurePermissionsService;
import de.bennyboer.author.server.structure.persistence.lookup.StructureLookupRepo;
import de.bennyboer.author.server.structure.transformer.StructureTransformer;
import de.bennyboer.author.structure.StructureId;
import de.bennyboer.author.structure.StructureService;
import lombok.AllArgsConstructor;
import lombok.Value;
import reactor.core.publisher.Mono;

import static de.bennyboer.author.server.structure.permissions.StructureAction.READ;

@Value
@AllArgsConstructor
public class StructureQueryFacade {

    StructureService structureService;

    StructurePermissionsService permissionsService;

    StructureLookupRepo lookupRepo;

    public Mono<StructureDTO> getStructure(String id, Agent agent) {
        StructureId structureId = StructureId.of(id);

        return permissionsService.assertHasPermission(agent, READ, structureId)
                .then(structureService.get(structureId))
                .map(StructureTransformer::toApi);
    }

    public Mono<String> findStructureIdByProjectId(String projectId, Agent agent) {
        return lookupRepo.findStructureIdByProjectId(projectId)
                .flatMap(structureId -> permissionsService.assertHasPermission(agent, READ, structureId)
                        .thenReturn(structureId.getValue()));
    }

}
