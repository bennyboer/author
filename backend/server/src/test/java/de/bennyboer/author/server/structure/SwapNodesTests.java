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

public class SwapNodesTests extends StructureModuleTests {

    @Test
    void shouldSwapNodes() {
        String projectId = "PROJECT_ID";

        JavalinTest.test(getJavalin(), (server, client) -> {
            // given: a structure with two nodes under the root node
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
            addNode(
                    client,
                    correctToken,
                    structureId,
                    structure.getVersion() + 1,
                    structure.getRootNodeId(),
                    "Chapter 2"
            );
            structure = getStructure(client, correctToken, structureId).getStructure();
            NodeDTO rootNode = structure.getNodes().get(structure.getRootNodeId());
            String chapter1NodeId = rootNode.getChildren().get(0);
            String chapter2NodeId = rootNode.getChildren().get(1);

            // when: swapping the nodes under the root node
            int statusCode = swapNodes(
                    client,
                    correctToken,
                    structureId,
                    structure.getVersion(),
                    chapter1NodeId,
                    chapter2NodeId
            );

            // then: the status code is 204
            assertThat(statusCode).isEqualTo(204);

            // and: the structure has been updated
            StructureDTO updatedStructure = getStructure(client, correctToken, structureId).getStructure();
            rootNode = updatedStructure.getNodes().get(structure.getRootNodeId());
            assertThat(rootNode.getChildren()).containsExactly(chapter2NodeId, chapter1NodeId);
        });
    }

    @Test
    void shouldNotBeAbleToSwapNodesGivenAnIncorrectToken() {
        String projectId = "PROJECT_ID";

        JavalinTest.test(getJavalin(), (server, client) -> {
            // given: a structure with two nodes under the root node
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
            addNode(
                    client,
                    correctToken,
                    structureId,
                    structure.getVersion() + 1,
                    structure.getRootNodeId(),
                    "Chapter 2"
            );
            structure = getStructure(client, correctToken, structureId).getStructure();
            NodeDTO rootNode = structure.getNodes().get(structure.getRootNodeId());
            String chapter1NodeId = rootNode.getChildren().get(0);
            String chapter2NodeId = rootNode.getChildren().get(1);

            // when: swapping the nodes under the root node with an incorrect token
            int statusCode = swapNodes(
                    client,
                    incorrectToken,
                    structureId,
                    structure.getVersion(),
                    chapter1NodeId,
                    chapter2NodeId
            );

            // then: the status code is 401
            assertThat(statusCode).isEqualTo(401);

            // and: the structure has not been updated
            StructureDTO updatedStructure = getStructure(client, correctToken, structureId).getStructure();
            rootNode = updatedStructure.getNodes().get(structure.getRootNodeId());
            assertThat(rootNode.getChildren()).containsExactly(chapter1NodeId, chapter2NodeId);
        });
    }

    @Test
    void shouldReceiveEventOverWebSocketWhenSwappedNodes() {
        String projectId = "PROJECT_ID";

        JavalinTest.test(getJavalin(), (server, client) -> {
            // given: a structure with two nodes under the root node
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
            addNode(
                    client,
                    correctToken,
                    structureId,
                    structure.getVersion() + 1,
                    structure.getRootNodeId(),
                    "Chapter 2"
            );
            structure = getStructure(client, correctToken, structureId).getStructure();
            NodeDTO rootNode = structure.getNodes().get(structure.getRootNodeId());
            String chapter1NodeId = rootNode.getChildren().get(0);
            String chapter2NodeId = rootNode.getChildren().get(1);

            // when: listening for events
            var eventReceived = getLatchForAwaitingEventOverWebSocket(
                    client,
                    correctToken,
                    Structure.TYPE,
                    AggregateId.of(structureId),
                    StructureEvent.NODES_SWAPPED.getName()
            );

            // and: swapping the nodes under the root node
            swapNodes(
                    client,
                    correctToken,
                    structureId,
                    structure.getVersion(),
                    chapter1NodeId,
                    chapter2NodeId
            );

            // then: the event has been received
            assertThat(eventReceived.await(5, TimeUnit.SECONDS)).isTrue();
        });
    }

}
