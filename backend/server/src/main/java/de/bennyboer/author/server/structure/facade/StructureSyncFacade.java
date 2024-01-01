package de.bennyboer.author.server.structure.facade;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.author.server.structure.external.project.ProjectDetailsService;
import de.bennyboer.author.server.structure.persistence.lookup.LookupStructure;
import de.bennyboer.author.server.structure.persistence.lookup.StructureLookupRepo;
import de.bennyboer.author.structure.Structure;
import de.bennyboer.author.structure.StructureId;
import de.bennyboer.author.structure.StructureService;
import de.bennyboer.author.structure.nodes.NodeName;
import lombok.AllArgsConstructor;
import lombok.Value;
import reactor.core.publisher.Mono;

@Value
@AllArgsConstructor
public class StructureSyncFacade {

    StructureService structureService;

    StructureLookupRepo lookupRepo;

    ProjectDetailsService projectDetailsService;

    public Mono<Void> create(String projectId, UserId userId) {
        return projectDetailsService.getProjectName(projectId)
                .map(NodeName::of)
                .flatMap(rootNodeName -> structureService.create(projectId, rootNodeName, Agent.user(userId)))
                .then();
    }

    public Mono<Void> removeStructureByProjectId(String projectId, Agent agent) {
        return lookupRepo.findStructureIdByProjectId(projectId)
                .flatMap(structureId -> structureService.remove(structureId, agent))
                .then();
    }

    public Mono<Void> addToLookup(StructureId structureId) {
        return structureService.get(structureId)
                .map(this::toLookupStructure)
                .flatMap(lookupRepo::update)
                .then();
    }

    public Mono<Void> removeFromLookup(StructureId structureId) {
        return lookupRepo.remove(structureId);
    }

    private LookupStructure toLookupStructure(Structure structure) {
        return LookupStructure.of(structure.getId(), structure.getProjectId());
    }

}
