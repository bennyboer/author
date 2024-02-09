package de.bennyboer.author.server.projects;

import de.bennyboer.author.server.projects.api.ProjectDTO;
import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class QueryProjectsTests extends ProjectsPluginTests {

    @Test
    void shouldQueryAccessibleProjects() {
        JavalinTest.test(getJavalin(), ((server, client) -> {
            // given: a user is created that is allowed to create projects
            userIsCreatedThatIsAllowedToCreateProjects();

            // and: a project is created
            createProjectAndAwaitCreation(client, "Test Project", correctToken);

            // when: querying the projects
            var response = getAccessibleProjects(client, correctToken);

            // then: the server responds with 200
            assertThat(response.getStatusCode()).isEqualTo(200);

            // and: the project is returned
            List<ProjectDTO> projects = response.getProjects();
            assertThat(projects.size()).isEqualTo(1);
            assertThat(projects.get(0).getName()).isEqualTo("Test Project");

            // when: querying the projects with another user that does not have access to the project
            var response2 = getAccessibleProjects(client, correctTokenForAnotherUser);

            // then: the server responds with 200
            assertThat(response2.getStatusCode()).isEqualTo(200);

            // and: no projects are returned
            List<ProjectDTO> projects2 = response2.getProjects();
            assertThat(projects2.isEmpty()).isTrue();
        }));
    }

}
