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

public class RemoveNodeTests extends StructureModuleTests {

    @Test
    void shouldRemoveNode() {
        String projectId = "PROJECT_ID";

        JavalinTest.test(getJavalin(), (server, client) -> {
            // given: a structure with a node under the root node
            projectAndItsCorrespondingStructureHaveBeenCreated(projectId, "Alice in Wonderland");
            String structureId = getStructureIdByProjectId(client, correctToken, projectId).getStructureId();
            StructureDTO structure = getStructure(client, correctToken, structureId).getStructure();
            addNode(
                    client,
                    correctToken,
                    structureId,
                    structure.getVersion(),
                    structure.getRootNodeId(),
                    "Chapter 1"
            );
            structure = getStructure(client, correctToken, structureId).getStructure();
            NodeDTO rootNode = structure.getNodes().get(structure.getRootNodeId());
            String nodeIdToRemove = rootNode.getChildren().get(0);

            // when: removing the node
            int statusCode = removeNode(
                    client,
                    correctToken,
                    structureId,
                    structure.getVersion(),
                    nodeIdToRemove
            );

            // then: the status code is 204
            assertThat(statusCode).isEqualTo(204);

            // and: the structure has been updated
            StructureDTO updatedStructure = getStructure(client, correctToken, structureId).getStructure();
            rootNode = updatedStructure.getNodes().get(structure.getRootNodeId());
            assertThat(rootNode.getChildren()).isEmpty();
        });
    }

    @Test
    void shouldNotBeAbleToRemoveNodeGivenAnIncorrectToken() {
        String projectId = "PROJECT_ID";

        JavalinTest.test(getJavalin(), (server, client) -> {
            // given: a structure with a node under the root node
            projectAndItsCorrespondingStructureHaveBeenCreated(projectId, "Alice in Wonderland");
            String structureId = getStructureIdByProjectId(client, correctToken, projectId).getStructureId();
            StructureDTO structure = getStructure(client, correctToken, structureId).getStructure();
            addNode(
                    client,
                    correctToken,
                    structureId,
                    structure.getVersion(),
                    structure.getRootNodeId(),
                    "Chapter 1"
            );
            structure = getStructure(client, correctToken, structureId).getStructure();
            NodeDTO rootNode = structure.getNodes().get(structure.getRootNodeId());
            String nodeIdToRemove = rootNode.getChildren().get(0);

            // when: removing the node
            int statusCode = removeNode(
                    client,
                    incorrectToken,
                    structureId,
                    structure.getVersion(),
                    nodeIdToRemove
            );

            // then: the status code is 401
            assertThat(statusCode).isEqualTo(401);

            // and: the structure has not been updated
            StructureDTO updatedStructure = getStructure(client, correctToken, structureId).getStructure();
            rootNode = updatedStructure.getNodes().get(structure.getRootNodeId());
            assertThat(rootNode.getChildren()).isNotEmpty();
        });
    }

    @Test
    void shouldNotBeAbleToRemoveTheRootNode() {
        String projectId = "PROJECT_ID";

        JavalinTest.test(getJavalin(), (server, client) -> {
            // given: a structure with a node under the root node
            projectAndItsCorrespondingStructureHaveBeenCreated(projectId, "Alice in Wonderland");
            String structureId = getStructureIdByProjectId(client, correctToken, projectId).getStructureId();
            StructureDTO structure = getStructure(client, correctToken, structureId).getStructure();

            // when: removing the root node
            int statusCode = removeNode(
                    client,
                    correctToken,
                    structureId,
                    structure.getVersion(),
                    structure.getRootNodeId()
            );

            // then: the status code is 500
            assertThat(statusCode).isEqualTo(500);

            // and: the structure has not been updated
            StructureDTO updatedStructure = getStructure(client, correctToken, structureId).getStructure();
            assertThat(updatedStructure.getNodes()).isNotEmpty();
        });
    }

    @Test
    void shouldReceiveEventViaWebSocketWhenNodeIsRemoved() {
        String projectId = "PROJECT_ID";

        JavalinTest.test(getJavalin(), (server, client) -> {
            // given: a structure with a node under the root node
            projectAndItsCorrespondingStructureHaveBeenCreated(projectId, "Alice in Wonderland");
            String structureId = getStructureIdByProjectId(client, correctToken, projectId).getStructureId();
            StructureDTO structure = getStructure(client, correctToken, structureId).getStructure();
            addNode(
                    client,
                    correctToken,
                    structureId,
                    structure.getVersion(),
                    structure.getRootNodeId(),
                    "Chapter 1"
            );
            structure = getStructure(client, correctToken, structureId).getStructure();
            NodeDTO rootNode = structure.getNodes().get(structure.getRootNodeId());
            String nodeIdToRemove = rootNode.getChildren().get(0);

            // when: listening to web socket events
            var eventReceived = getLatchForAwaitingEventOverWebSocket(
                    client,
                    correctToken,
                    Structure.TYPE,
                    AggregateId.of(structureId),
                    StructureEvent.NODE_REMOVED.getName()
            );

            // and: removing the node
            removeNode(
                    client,
                    correctToken,
                    structureId,
                    structure.getVersion(),
                    nodeIdToRemove
            );

            // then: the event has been received
            assertThat(eventReceived.await(5, TimeUnit.SECONDS)).isTrue();
        });
    }

}
