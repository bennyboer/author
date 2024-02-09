package de.bennyboer.author.server.structure;

import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PermissionsTests extends StructurePluginTests {

    @Test
    void shouldRemovePermissionsWhenUserIsRemoved() {
        JavalinTest.test(getJavalin(), (server, client) -> {
            // given: a project and its structure has been created
            projectAndItsCorrespondingStructureHaveBeenCreated("PROJECT_ID", "Alice in Wonderland");

            // and: the structure ID of the structure of the project
            String structureId = getStructureIdByProjectId(client, correctToken, "PROJECT_ID").getStructureId();

            // when: the logged in user is removed and the permissions of the structure are updated
            loggedInUserIsRemovedAndStructurePermissionsUpdated(structureId);

            // then: the structure cannot be queried with the correct token
            int statusCode = getStructure(client, correctToken, structureId).getStatusCode();
            assertThat(statusCode).isEqualTo(403);
        });
    }

}
