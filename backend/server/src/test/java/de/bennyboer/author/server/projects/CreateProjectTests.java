package de.bennyboer.author.server.projects;

import de.bennyboer.author.permissions.Action;
import de.bennyboer.author.project.Project;
import de.bennyboer.author.server.projects.permissions.ProjectAction;
import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateProjectTests extends ProjectsModuleTests {

    @Test
    void shouldCreateProject() {
        JavalinTest.test(getJavalin(), (server, client) -> {
            // given: a user is created that is allowed to create projects
            userIsCreatedThatIsAllowedToCreateProjects();

            // when: creating a project with a correct token
            var response = createProject(client, "Test Project", correctToken);

            // then: the server responds with 204
            assertThat(response.getStatusCode()).isEqualTo(204);

            // and: with the ID of the created project
            assertThat(response.getProjectId()).isNotNull();
        });
    }

    @Test
    void shouldNotCreateProjectGivenIncorrectToken() {
        JavalinTest.test(getJavalin(), (server, client) -> {
            // given: a user is created that is allowed to create projects
            userIsCreatedThatIsAllowedToCreateProjects();

            // when: creating a project with an incorrect token
            var response = createProject(client, "Test Project", incorrectToken);

            // then: the server responds with 401
            assertThat(response.getStatusCode()).isEqualTo(401);
        });
    }

    @Test
    void shouldNotCreateProjectGivenMissingPermissions() {
        JavalinTest.test(getJavalin(), (server, client) -> {
            // given: a user is created that is allowed to create projects
            userIsCreatedThatIsNotAllowedToCreateProjects();

            // when: creating a project with missing permissions
            var response = createProject(client, "Test Project", correctToken);

            // then: the server responds with 403
            assertThat(response.getStatusCode()).isEqualTo(403);
        });
    }

    @Test
    void shouldReceiveWebSocketPermissionEventWhenCreatingProject() {
        JavalinTest.test(getJavalin(), (server, client) -> {
            // given: a user is created that is allowed to create projects
            userIsCreatedThatIsAllowedToCreateProjects();

            // when: listening to permission events on the project aggregate for the currently authenticated user
            var eventReceived = getLatchForAwaitingPermissionEventOverWebSocket(
                    client,
                    correctToken,
                    Project.TYPE,
                    null,
                    Action.of(ProjectAction.READ.name())
            );

            // and: a project is created
            createProject(client, "Test Project", correctToken);

            // then: the event is received
            assertThat(eventReceived.await(5, TimeUnit.SECONDS)).isTrue();
        });
    }

    @Test
    void shouldNotReceiveWebSocketPermissionEventWhenUnsubscribing() {
        JavalinTest.test(getJavalin(), (server, client) -> {
            // given: a user is created that is allowed to create projects
            userIsCreatedThatIsAllowedToCreateProjects();

            // when: listening to permission events on the project aggregate for the currently authenticated user
            var eventReceived = getLatchForAwaitingPermissionEventOverWebSocket(
                    client,
                    correctToken,
                    Project.TYPE,
                    null,
                    Action.of(ProjectAction.READ.name())
            );

            // and: unsubscribing from the event
            eventReceived.unsubscribe();

            // and: a project is created
            createProject(client, "Test Project", correctToken);

            // then: the event is not received
            assertThat(eventReceived.await(1, TimeUnit.SECONDS)).isFalse();
        });
    }

}
