package de.bennyboer.author.server.projects.persistence.lookup;

import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.project.ProjectId;
import de.bennyboer.author.project.ProjectName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class ProjectLookupRepoTests {

    private final ProjectLookupRepo repo = createRepo();

    protected abstract ProjectLookupRepo createRepo();

    @Test
    void shouldInsertProject() {
        // given: a project to update
        var project = LookupProject.of(
                ProjectId.of("PROJECT_ID_1"),
                Version.of(1),
                ProjectName.of("Project 1"),
                Instant.now()
        );

        // when: the project is updated
        repo.update(project).block();

        // then: the project is updated
        var saved = repo.getProjects(Set.of(ProjectId.of("PROJECT_ID_1"))).blockFirst();
        assertThat(saved).usingRecursiveAssertion()
                .isEqualTo(project)
                .ignoringFields("createdAt");
    }

    @Test
    void shouldUpdateProject() {
        // given: a project
        var project = LookupProject.of(
                ProjectId.of("PROJECT_ID_1"),
                Version.of(1),
                ProjectName.of("Project 1"),
                Instant.now()
        );
        repo.update(project).block();

        // when: the project is updated
        var updatedProject = LookupProject.of(
                ProjectId.of("PROJECT_ID_1"),
                Version.of(2),
                ProjectName.of("Project 1"),
                Instant.now()
        );

        repo.update(updatedProject).block();

        // then: the project is updated
        var saved = repo.getProjects(Set.of(ProjectId.of("PROJECT_ID_1"))).blockFirst();
        assertThat(saved).usingRecursiveAssertion()
                .isEqualTo(updatedProject)
                .ignoringFields("createdAt");
    }

    @Test
    void shouldRemoveProject() {
        // given: a project to remove
        var project = LookupProject.of(
                ProjectId.of("PROJECT_ID_1"),
                Version.of(1),
                ProjectName.of("Project 1"),
                Instant.now()
        );
        repo.update(project).block();

        // when: the project is removed
        repo.remove(ProjectId.of("PROJECT_ID_1")).block();

        // then: the project is removed
        var saved = repo.getProjects(Set.of(ProjectId.of("PROJECT_ID_1"))).blockFirst();
        assertThat(saved).isNull();
    }

    @Test
    void shouldFetchMultipleProjects() {
        // given: some projects
        var project1 = LookupProject.of(
                ProjectId.of("PROJECT_ID_1"),
                Version.of(1),
                ProjectName.of("Project 1"),
                Instant.now()
        );
        var project2 = LookupProject.of(
                ProjectId.of("PROJECT_ID_2"),
                Version.of(1),
                ProjectName.of("Project 2"),
                Instant.now()
        );
        var project3 = LookupProject.of(
                ProjectId.of("PROJECT_ID_3"),
                Version.of(1),
                ProjectName.of("Project 3"),
                Instant.now()
        );

        repo.update(project1).block();
        repo.update(project2).block();
        repo.update(project3).block();

        // when: two of the projects are fetched
        var found = repo.getProjects(Set.of(
                ProjectId.of("PROJECT_ID_1"),
                ProjectId.of("PROJECT_ID_3")
        )).collectList().block();

        // then: the two projects are fetched
        assertThat(found.size()).isEqualTo(2);
        assertThat(found).usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(project1, project3);
    }

}
