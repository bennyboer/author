package de.bennyboer.author.persistence.patches;

import de.bennyboer.author.persistence.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PatchManager<P extends RepositoryPatch> {

    private final List<P> patches;

    public PatchManager(List<P> patches) {
        this.patches = patches;
    }

    public PatchManager() {
        this(new ArrayList<>());
    }

    public void registerPatch(P patch) {
        patches.add(patch);
    }

    /**
     * Find all patches that have not been applied yet in correct order (lowest version first).
     */
    public Mono<List<P>> findUnappliedPatches(Repository repository) {
        return repository.getVersion()
                .flatMapMany(version -> Flux.fromIterable(patches)
                        .filter(patch -> patch.appliesTo().isGreaterThanOrEqualTo(version)))
                .collectSortedList(Comparator.comparing(RepositoryPatch::appliesTo));
    }

}
