package de.bennyboer.author.server.structure;

import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RemoveStructureTests extends StructureModuleTests {

    @Test
    void shouldRemoveStructureWhenProjectIsRemoved() {
        String projectId = "PROJECT_ID";

        JavalinTest.test(getJavalin(), (server, client) -> {
            // given: a project and its structure has been created
            projectAndItsCorrespondingStructureHaveBeenCreated(projectId, "Alice in Wonderland");

            // and: the structure ID of the structure of the project
            String structureId = getStructureIdByProjectId(client, correctToken, projectId).getStructureId();

            // when: the project is removed
            projectHasBeenRemoved(projectId);

            // and: we wait for the structure to be removed
            awaitStructureToBeRemoved(structureId);

            // then: the structure cannot be queried with the correct token
            var response = getStructure(client, correctToken, structureId);
            assertThat(response.getStructure()).isNull();
        });
    }

}
