package de.bennyboer.author.server.structure.persistence.lookup;

import de.bennyboer.author.structure.tree.Tree;
import de.bennyboer.author.structure.tree.TreeId;
import reactor.core.publisher.Mono;

public interface TreeLookupRepo {

    Mono<TreeId> findTreeIdByProjectId(String projectId);

    Mono<Void> update(Tree tree);

    Mono<Void> remove(TreeId treeId);

}
