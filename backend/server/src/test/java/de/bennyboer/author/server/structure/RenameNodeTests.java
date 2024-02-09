package de.bennyboer.author.server.structure;

import de.bennyboer.author.eventsourcing.aggregate.AggregateId;
import de.bennyboer.author.server.structure.api.NodeDTO;
import de.bennyboer.author.server.structure.api.StructureDTO;
import de.bennyboer.author.structure.Structure;
import de.bennyboer.author.structure.StructureEvent;
import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class RenameNodeTests extends StructurePluginTests {

    @Test
    void shouldRenameNode() {
        String projectId = "PROJECT_ID";

        JavalinTest.test(getJavalin(), (server, client) -> {
            // given: a structure
            projectAndItsCorrespondingStructureHaveBeenCreated(projectId, "Alice in Wonderland");
            String structureId = getStructureIdByProjectId(client, correctToken, projectId).getStructureId();
            StructureDTO structure = getStructure(client, correctToken, structureId).getStructure();

            // when: renaming the root node
            int statusCode = renameNode(
                    client,
                    correctToken,
                    structureId,
                    structure.getVersion(),
                    structure.getRootNodeId(),
                    "New Name"
            );

            // then: the status code is 204
            assertThat(statusCode).isEqualTo(204);

            // and: the structure has been updated
            StructureDTO updatedStructure = getStructure(client, correctToken, structureId).getStructure();
            NodeDTO rootNode = updatedStructure.getNodes().get(structure.getRootNodeId());
            assertThat(rootNode.getName()).isEqualTo("New Name");
        });
    }

    @Test
    void shouldNotAllowRenamingNodeGivenAnIncorrectToken() {
        String projectId = "PROJECT_ID";

        JavalinTest.test(getJavalin(), (server, client) -> {
            // given: a structure
            projectAndItsCorrespondingStructureHaveBeenCreated(projectId, "Alice in Wonderland");
            String structureId = getStructureIdByProjectId(client, correctToken, projectId).getStructureId();
            StructureDTO structure = getStructure(client, correctToken, structureId).getStructure();

            // when: renaming the root node with an incorrect token
            int statusCode = renameNode(
                    client,
                    incorrectToken,
                    structureId,
                    structure.getVersion(),
                    structure.getRootNodeId(),
                    "New Name"
            );

            // then: the status code is 401
            assertThat(statusCode).isEqualTo(401);
        });
    }

    @Test
    void shouldReceiveEventOverWebsocketWhenRenamingNode() {
        String projectId = "PROJECT_ID";

        JavalinTest.test(getJavalin(), (server, client) -> {
            // given: a structure
            projectAndItsCorrespondingStructureHaveBeenCreated(projectId, "Alice in Wonderland");
            String structureId = getStructureIdByProjectId(client, correctToken, projectId).getStructureId();
            StructureDTO structure = getStructure(client, correctToken, structureId).getStructure();

            // when: listening to events
            var eventReceived = getLatchForAwaitingEventOverWebSocket(
                    client,
                    correctToken,
                    Structure.TYPE,
                    AggregateId.of(structureId),
                    StructureEvent.NODE_RENAMED.getName()
            );

            // when: renaming the root node
            renameNode(
                    client,
                    correctToken,
                    structureId,
                    structure.getVersion(),
                    structure.getRootNodeId(),
                    "New Name"
            );

            // then: the event has been received
            assertThat(eventReceived.await(5, TimeUnit.SECONDS)).isTrue();
        });
    }

}
