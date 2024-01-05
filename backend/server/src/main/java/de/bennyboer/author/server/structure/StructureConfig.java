package de.bennyboer.author.server.structure;

import de.bennyboer.author.eventsourcing.persistence.EventSourcingRepo;
import de.bennyboer.author.permissions.repo.PermissionsRepo;
import de.bennyboer.author.server.structure.external.project.ProjectDetailsService;
import de.bennyboer.author.server.structure.persistence.lookup.StructureLookupRepo;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class StructureConfig {

    EventSourcingRepo eventSourcingRepo;

    PermissionsRepo permissionsRepo;

    StructureLookupRepo structureLookupRepo;

    ProjectDetailsService projectDetailsService;

}
