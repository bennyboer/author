package de.bennyboer.author.server.structure.persistence.lookup;

import de.bennyboer.author.structure.tree.Tree;
import de.bennyboer.author.structure.tree.TreeId;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryTreeLookupRepo implements TreeLookupRepo {

    private final Map<String, TreeId> projectIdToTreeId = new ConcurrentHashMap<>();

    @Override
    public Mono<TreeId> findTreeIdByProjectId(String projectId) {
        return Mono.justOrEmpty(projectIdToTreeId.get(projectId));
    }

    @Override
    public Mono<Void> update(Tree tree) {
        return Mono.fromRunnable(() -> projectIdToTreeId.put(tree.getProjectId(), tree.getId()));
    }

    @Override
    public Mono<Void> remove(TreeId treeId) {
        return Mono.fromRunnable(() -> {
            for (Map.Entry<String, TreeId> entry : projectIdToTreeId.entrySet()) {
                if (entry.getValue().equals(treeId)) {
                    projectIdToTreeId.remove(entry.getKey());
                }
            }
        });
    }

}
