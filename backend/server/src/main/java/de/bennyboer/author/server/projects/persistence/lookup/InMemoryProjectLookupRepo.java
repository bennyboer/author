package de.bennyboer.author.server.projects.persistence.lookup;

import de.bennyboer.author.eventsourcing.persistence.readmodel.InMemoryEventSourcingReadModelRepo;
import de.bennyboer.author.project.ProjectId;
import reactor.core.publisher.Flux;

import java.util.Collection;

public class InMemoryProjectLookupRepo extends InMemoryEventSourcingReadModelRepo<ProjectId, LookupProject>
        implements ProjectLookupRepo {

    @Override
    public Flux<LookupProject> getProjects(Collection<ProjectId> ids) {
        return Flux.fromIterable(ids)
                .mapNotNull(lookup::get);
    }

    @Override
    protected ProjectId getId(LookupProject readModel) {
        return readModel.getId();
    }

}
