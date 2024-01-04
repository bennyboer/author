package de.bennyboer.author.server.projects;

import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateProjectTests extends ProjectsModuleTests {

    @Test
    void shouldCreateProject() {
        JavalinTest.test(javalin, (server, client) -> {
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
        JavalinTest.test(javalin, (server, client) -> {
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
        JavalinTest.test(javalin, (server, client) -> {
            // given: a user is created that is allowed to create projects
            userIsCreatedThatIsNotAllowedToCreateProjects();

            // when: creating a project with missing permissions
            var response = createProject(client, "Test Project", correctToken);

            // then: the server responds with 403
            assertThat(response.getStatusCode()).isEqualTo(403);
        });
    }

}
