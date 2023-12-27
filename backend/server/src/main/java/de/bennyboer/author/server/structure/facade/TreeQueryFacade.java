package de.bennyboer.author.server.structure.facade;

import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.author.server.structure.api.TreeDTO;
import de.bennyboer.author.server.structure.permissions.TreePermissionsService;
import de.bennyboer.author.server.structure.persistence.lookup.TreeLookupRepo;
import de.bennyboer.author.server.structure.transformer.TreeTransformer;
import de.bennyboer.author.structure.tree.TreeId;
import de.bennyboer.author.structure.tree.TreeService;
import lombok.AllArgsConstructor;
import lombok.Value;
import reactor.core.publisher.Mono;

import static de.bennyboer.author.server.structure.permissions.TreeAction.READ;

@Value
@AllArgsConstructor
public class TreeQueryFacade {

    TreeService treeService;

    TreePermissionsService permissionsService;

    TreeLookupRepo lookupRepo;

    public Mono<TreeDTO> getTree(String id, Agent agent) {
        TreeId treeId = TreeId.of(id);

        return permissionsService.assertHasPermission(agent, READ, treeId)
                .then(treeService.get(treeId))
                .map(TreeTransformer::toApi);
    }

    public Mono<String> findTreeIdByProjectId(String projectId, Agent agent) {
        return lookupRepo.findTreeIdByProjectId(projectId)
                .flatMap(treeId -> permissionsService.assertHasPermission(agent, READ, treeId)
                        .thenReturn(treeId.getValue()));
    }

}
