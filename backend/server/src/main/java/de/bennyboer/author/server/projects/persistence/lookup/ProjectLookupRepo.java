package de.bennyboer.author.server.projects.persistence.lookup;

import de.bennyboer.author.eventsourcing.persistence.readmodel.EventSourcingReadModelRepo;
import de.bennyboer.author.project.ProjectId;
import reactor.core.publisher.Flux;

import java.util.Collection;

public interface ProjectLookupRepo extends EventSourcingReadModelRepo<ProjectId, LookupProject> {

    Flux<LookupProject> getProjects(Collection<ProjectId> ids);

}
