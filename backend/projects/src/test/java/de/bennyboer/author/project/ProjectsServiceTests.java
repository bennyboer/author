package de.bennyboer.author.project;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.author.eventsourcing.persistence.InMemoryEventSourcingRepo;
import de.bennyboer.author.eventsourcing.testing.TestEventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.*;

public class ProjectsServiceTests {

    private final ProjectsService projectsService = new ProjectsService(
            new InMemoryEventSourcingRepo(),
            new TestEventPublisher()
    );

    private final Agent testAgent = Agent.user(UserId.of("TEST_USER_ID"));

    @Test
    void shouldCreateProject() {
        // given: the name of the project to be created
        var name = ProjectName.of("Alice in Wonderland");

        // when: a project is created
        var projectIdAndVersion = projectsService.create(name, testAgent).block();
        var projectId = projectIdAndVersion.getId();
        var version = projectIdAndVersion.getVersion();

        // then: the project can be retrieved
        var project = projectsService.get(projectId, version).block();
        assertEquals(name, project.getName());
    }

    @Test
    void shouldRenameProject() {
        // given: a project
        var originalName = ProjectName.of("Alice in Wonderland");
        var projectIdAndVersion = projectsService.create(originalName, testAgent).block();
        var projectId = projectIdAndVersion.getId();
        var version = projectIdAndVersion.getVersion();

        // when: the project is renamed
        var newName = ProjectName.of("Alice in the land of horrors");
        projectsService.rename(projectId, version, newName, testAgent).block();

        // then: the project name has changed
        var project = projectsService.get(projectId).block();
        assertEquals(newName, project.getName());
    }

    @Test
    void shouldRemoveProject() {
        // given: a project
        var name = ProjectName.of("Alice in Wonderland");
        var projectIdAndVersion = projectsService.create(name, testAgent).block();
        var projectId = projectIdAndVersion.getId();
        var version = projectIdAndVersion.getVersion();

        // when: the project is removed
        projectsService.remove(projectId, version, testAgent).block();

        // then: the project is gone
        var project = projectsService.get(projectId).block();
        assertNull(project);
    }

    @Test
    void shouldNotAcceptOtherCommandBeforeCreating() {
        // when: trying to rename a non-existing project
        Executable executable = () -> projectsService.rename(
                ProjectId.create(),
                Version.zero(),
                ProjectName.of("Alice in Wonderland"),
                testAgent
        ).block();

        // then: an exception is thrown
        var exception = assertThrows(
                IllegalStateException.class,
                executable
        );
        assertEquals(
                "Project must be initialized with CreateCmd before applying other commands",
                exception.getMessage()
        );
    }

    @Test
    void shouldNotAcceptCommandsAfterRemoval() {
        // given: a removed project
        var name = ProjectName.of("Alice in Wonderland");
        var projectIdAndVersion = projectsService.create(name, testAgent).block();
        var projectId = projectIdAndVersion.getId();
        var initialVersion = projectIdAndVersion.getVersion();
        var version = projectsService.remove(projectId, initialVersion, testAgent).block();

        // when: trying to rename the removed project
        Executable executable = () -> projectsService.rename(
                projectId,
                version,
                ProjectName.of("Alice in the land of horrors"),
                testAgent
        ).block();

        // then: an exception is thrown
        var exception = assertThrows(
                IllegalStateException.class,
                executable
        );
        assertEquals(
                "Cannot apply command to removed Project",
                exception.getMessage()
        );
    }

}
