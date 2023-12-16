package de.bennyboer.author.project;

import de.bennyboer.common.UserId;
import de.bennyboer.eventsourcing.Version;
import de.bennyboer.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.eventsourcing.persistence.InMemoryEventSourcingRepo;
import de.bennyboer.eventsourcing.testing.TestEventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.*;

public class ProjectServiceTests {

    private final ProjectService projectService = new ProjectService(
            new InMemoryEventSourcingRepo(),
            new TestEventPublisher()
    );

    private final Agent testAgent = Agent.user(UserId.of("TEST_USER_ID"));

    @Test
    void shouldCreateProject() {
        // given: the name of the project to be created
        var name = ProjectName.of("Alice in Wonderland");

        // when: a project is created
        var projectIdAndVersion = projectService.create(name, testAgent).block();
        var projectId = projectIdAndVersion.getId();
        var version = projectIdAndVersion.getVersion();

        // then: the project can be retrieved
        var project = projectService.get(projectId, version).block();
        assertEquals(name, project.getName());
    }

    @Test
    void shouldRenameProject() {
        // given: a project
        var originalName = ProjectName.of("Alice in Wonderland");
        var projectIdAndVersion = projectService.create(originalName, testAgent).block();
        var projectId = projectIdAndVersion.getId();
        var version = projectIdAndVersion.getVersion();

        // when: the project is renamed
        var newName = ProjectName.of("Alice in the land of horrors");
        projectService.rename(projectId, version, newName, testAgent).block();

        // then: the project name has changed
        var project = projectService.get(projectId).block();
        assertEquals(newName, project.getName());
    }

    @Test
    void shouldRemoveProject() {
        // given: a project
        var name = ProjectName.of("Alice in Wonderland");
        var projectIdAndVersion = projectService.create(name, testAgent).block();
        var projectId = projectIdAndVersion.getId();
        var version = projectIdAndVersion.getVersion();

        // when: the project is removed
        projectService.remove(projectId, version, testAgent).block();

        // then: the project is gone
        var project = projectService.get(projectId).block();
        assertNull(project);
    }

    @Test
    void shouldNotAcceptOtherCommandBeforeCreating() {
        // when: trying to rename a non-existing project
        Executable executable = () -> projectService.rename(
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
        var projectIdAndVersion = projectService.create(name, testAgent).block();
        var projectId = projectIdAndVersion.getId();
        var initialVersion = projectIdAndVersion.getVersion();
        var version = projectService.remove(projectId, initialVersion, testAgent).block();

        // when: trying to rename the removed project
        Executable executable = () -> projectService.rename(
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
