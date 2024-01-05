package de.bennyboer.author.server.projects;

import de.bennyboer.author.eventsourcing.aggregate.AggregateId;
import de.bennyboer.author.project.Project;
import de.bennyboer.author.project.ProjectEvent;
import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class RenameProjectTests extends ProjectsModuleTests {

    @Test
    void shouldRenameProject() {
        JavalinTest.test(getJavalin(), (server, client) -> {
            // given: a user is created that is allowed to create projects
            userIsCreatedThatIsAllowedToCreateProjects();

            // and: a project is created
            var projectId = createProjectAndAwaitCreation(client, "Test Project", correctToken);

            // and: the project is fetched
            var getProjectResponse = getProject(client, projectId, correctToken);
            var projectVersion = getProjectResponse.getProject().getVersion();

            // when: renaming the project with a correct token
            var statusCode = renameProject(client, projectId, projectVersion, "Renamed Project", correctToken);

            // then: the server responds with 204
            assertThat(statusCode).isEqualTo(204);

            // and: the project is renamed
            getProjectResponse = getProject(client, projectId, correctToken);
            assertThat(getProjectResponse.getProject().getName()).isEqualTo("Renamed Project");
        });
    }

    @Test
    void shouldNotBeAbleToRenameProjectWhenGivenAnIncorrectToken() {
        JavalinTest.test(getJavalin(), (server, client) -> {
            // given: a user is created that is allowed to create projects
            userIsCreatedThatIsAllowedToCreateProjects();

            // and: a project is created
            var projectId = createProjectAndAwaitCreation(client, "Test Project", correctToken);

            // and: the project is fetched
            var getProjectResponse = getProject(client, projectId, correctToken);
            var projectVersion = getProjectResponse.getProject().getVersion();

            // when: renaming the project with an incorrect token
            var statusCode = renameProject(client, projectId, projectVersion, "Renamed Project", incorrectToken);

            // then: the server responds with 401
            assertThat(statusCode).isEqualTo(401);

            // and: the project is not renamed
            getProjectResponse = getProject(client, projectId, correctToken);
            assertThat(getProjectResponse.getProject().getName()).isEqualTo("Test Project");
        });
    }

    @Test
    void shouldReceiveWebSocketMessageWhenProjectIsRenamed() {
        JavalinTest.test(getJavalin(), (server, client) -> {
            // given: a user is created that is allowed to create projects
            userIsCreatedThatIsAllowedToCreateProjects();

            // and: a project is created
            var projectId = createProjectAndAwaitCreation(client, "Test Project", correctToken);

            // when: listening to rename events
            CountDownLatch eventReceived = getLatchForAwaitingEventOverWebSocket(
                    client,
                    correctToken,
                    Project.TYPE,
                    AggregateId.of(projectId),
                    ProjectEvent.RENAMED.getName()
            );

            // and: renaming the project
            renameProject(client, projectId, 0, "Renamed Project", correctToken);

            // then: the event is received
            assertThat(eventReceived.await(5, TimeUnit.SECONDS)).isTrue();
        });
    }

}
