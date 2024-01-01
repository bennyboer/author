package de.bennyboer.author.server.projects.persistence.lookup;

import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.project.ProjectId;
import de.bennyboer.author.project.ProjectName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LookupProject {

    ProjectId id;

    Version version;

    ProjectName name;

    Instant createdAt;

    public static LookupProject of(ProjectId projectId, Version version, ProjectName name, Instant createdAt) {
        checkNotNull(projectId, "Project ID must be given");
        checkNotNull(version, "Version must be given");
        checkNotNull(name, "Name must be given");
        checkNotNull(createdAt, "Created at must be given");

        return new LookupProject(projectId, version, name, createdAt);
    }

}
