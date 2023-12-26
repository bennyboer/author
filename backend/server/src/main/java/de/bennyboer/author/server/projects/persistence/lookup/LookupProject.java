package de.bennyboer.author.server.projects.persistence.lookup;

import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.project.Project;
import de.bennyboer.author.project.ProjectId;
import de.bennyboer.author.project.ProjectName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LookupProject {

    ProjectId id;

    Version version;

    ProjectName name;

    Instant createdAt;

    public static LookupProject of(Project project) {
        return new LookupProject(
                project.getId(),
                project.getVersion(),
                project.getName(),
                project.getCreatedAt()
        );
    }

}
