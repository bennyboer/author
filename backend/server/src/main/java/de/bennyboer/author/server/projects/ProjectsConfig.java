package de.bennyboer.author.server.projects;

import de.bennyboer.author.eventsourcing.persistence.EventSourcingRepo;
import de.bennyboer.author.permissions.repo.PermissionsRepo;
import de.bennyboer.author.server.projects.persistence.lookup.ProjectLookupRepo;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ProjectsConfig {

    EventSourcingRepo eventSourcingRepo;

    PermissionsRepo permissionsRepo;

    ProjectLookupRepo projectLookupRepo;

}
