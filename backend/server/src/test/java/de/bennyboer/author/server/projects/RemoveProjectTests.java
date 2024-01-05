package de.bennyboer.author.server.projects;

import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RemoveProjectTests extends ProjectsModuleTests {

    @Test
    void shouldRemoveProject() {
        JavalinTest.test(getJavalin(), (server, client) -> {
            // given: a user is created that is allowed to create projects
            userIsCreatedThatIsAllowedToCreateProjects();

            // and: a project is created
            var projectId = createProjectAndAwaitCreation(client, "Test Project", correctToken);

            // and: the project is fetched
            var getProjectResponse = getProject(client, projectId, correctToken);
            var projectVersion = getProjectResponse.getProject().getVersion();

            // when: removing the project with a correct token
            var statusCode = removeProject(client, projectId, projectVersion, correctToken);

            // then: the server responds with 204
            assertThat(statusCode).isEqualTo(204);

            // and: the project is removed
            getProjectResponse = getProject(client, projectId, correctToken);
            assertThat(getProjectResponse.getProject()).isNull();
        });
    }

    @Test
    void shouldNotBeAbleToRemoveProjectGivenAnIncorrectAccessToken() {
        JavalinTest.test(getJavalin(), (server, client) -> {
            // given: a user is created that is allowed to create projects
            userIsCreatedThatIsAllowedToCreateProjects();

            // and: a project is created
            var projectId = createProjectAndAwaitCreation(client, "Test Project", correctToken);

            // and: the project is fetched
            var getProjectResponse = getProject(client, projectId, correctToken);
            var projectVersion = getProjectResponse.getProject().getVersion();

            // when: removing the project with an incorrect token
            var statusCode = removeProject(client, projectId, projectVersion, incorrectToken);

            // then: the server responds with 401
            assertThat(statusCode).isEqualTo(401);

            // and: the project is not removed
            getProjectResponse = getProject(client, projectId, correctToken);
            assertThat(getProjectResponse.getStatusCode()).isEqualTo(200);
        });
    }

}
