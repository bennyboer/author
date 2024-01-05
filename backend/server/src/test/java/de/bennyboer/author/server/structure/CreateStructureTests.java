package de.bennyboer.author.server.structure;

import de.bennyboer.author.server.structure.api.NodeDTO;
import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateStructureTests extends StructureModuleTests {

    @Test
    void shouldCreateStructureWhenProjectIsCreated() {
        String projectId = "PROJECT_ID";

        JavalinTest.test(getJavalin(), (server, client) -> {
            // given: a project has been created
            projectHasBeenCreated(projectId, "Alice in Wonderland");

            // when: we wait for the structure to be created
            awaitStructureToBeCreatedForProject(projectId);

            // then: the structure ID can be queried
            String structureId = getStructureIdByProjectId(client, correctToken, projectId).getStructureId();
            assertThat(structureId).isNotNull();

            // and: the structure cannot be queried with an incorrect token
            int statusCode = getStructureIdByProjectId(client, incorrectToken, projectId).getStatusCode();
            assertThat(statusCode).isEqualTo(401);

            // and: we can query the structure with the correct token
            var response = getStructure(client, correctToken, structureId);
            assertThat(response.getStatusCode()).isEqualTo(200);
            assertThat(response.getStructure()).isNotNull();
            var structure = response.getStructure();

            // and: we cannot query the structure with the incorrect token
            var invalidTokenResponse = getStructure(client, incorrectToken, structureId);
            assertThat(invalidTokenResponse.getStatusCode()).isEqualTo(401);

            // and: the structures root nodes name is the projects name
            NodeDTO rootNode = structure.getNodes().get(structure.getRootNodeId());
            assertThat(rootNode.getName()).isEqualTo("Alice in Wonderland");
        });
    }

}
