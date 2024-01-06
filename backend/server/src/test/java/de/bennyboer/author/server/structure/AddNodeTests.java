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

public class AddNodeTests extends StructureModuleTests {

    @Test
    void shouldAddNode() {
        String projectId = "PROJECT_ID";

        JavalinTest.test(getJavalin(), (server, client) -> {
            // given: a structure
            projectAndItsCorrespondingStructureHaveBeenCreated(projectId, "Alice in Wonderland");
            String structureId = getStructureIdByProjectId(client, correctToken, projectId).getStructureId();
            StructureDTO structure = getStructure(client, correctToken, structureId).getStructure();

            // when: adding a node
            int statusCode = addNodeChild(
                    client,
                    correctToken,
                    structureId,
                    structure.getVersion(),
                    structure.getRootNodeId(),
                    "Chapter 1"
            );

            // then: the status code is 204
            assertThat(statusCode).isEqualTo(204);

            // and: the structure has been updated
            StructureDTO updatedStructure = getStructure(client, correctToken, structureId).getStructure();
            NodeDTO rootNode = updatedStructure.getNodes().get(structure.getRootNodeId());
            assertThat(rootNode.getChildren()).hasSize(1);
            NodeDTO chapter1Node = updatedStructure.getNodes().get(rootNode.getChildren().get(0));
            assertThat(chapter1Node.getName()).isEqualTo("Chapter 1");
        });
    }

    @Test
    void shouldNotBeAbleToAddNodeWhenGivenAnIncorrectToken() {
        String projectId = "PROJECT_ID";

        JavalinTest.test(getJavalin(), (server, client) -> {
            // given: a structure
            projectAndItsCorrespondingStructureHaveBeenCreated(projectId, "Alice in Wonderland");
            String structureId = getStructureIdByProjectId(client, correctToken, projectId).getStructureId();
            StructureDTO structure = getStructure(client, correctToken, structureId).getStructure();

            // when: adding a node with an incorrect token
            int statusCode = addNodeChild(
                    client,
                    incorrectToken,
                    structureId,
                    structure.getVersion(),
                    structure.getRootNodeId(),
                    "Chapter 1"
            );

            // then: the status code is 401
            assertThat(statusCode).isEqualTo(401);

            // and: the structure has not been updated
            StructureDTO updatedStructure = getStructure(client, correctToken, structureId).getStructure();
            NodeDTO rootNode = updatedStructure.getNodes().get(structure.getRootNodeId());
            assertThat(rootNode.getChildren()).isEmpty();
        });
    }

    @Test
    void shouldPublishEventOverWebSocketWhenAddingNode() {
        String projectId = "PROJECT_ID";

        JavalinTest.test(getJavalin(), (server, client) -> {
            // given: a structure
            projectAndItsCorrespondingStructureHaveBeenCreated(projectId, "Alice in Wonderland");
            String structureId = getStructureIdByProjectId(client, correctToken, projectId).getStructureId();
            StructureDTO structure = getStructure(client, correctToken, structureId).getStructure();

            // when: listening to structure events over the web socket
            var eventReceived = getLatchForAwaitingEventOverWebSocket(
                    client,
                    correctToken,
                    Structure.TYPE,
                    AggregateId.of(structureId),
                    StructureEvent.NODE_ADDED.getName()
            );

            // and: adding a node
            addNodeChild(
                    client,
                    correctToken,
                    structureId,
                    structure.getVersion(),
                    structure.getRootNodeId(),
                    "Chapter 1"
            );

            // then: the event is received
            assertThat(eventReceived.await(5, TimeUnit.SECONDS)).isTrue();
        });
    }

    @Test
    void shouldNotReceiveEventOverWebSocketWhenUnsubscribed() {
        String projectId = "PROJECT_ID";

        JavalinTest.test(getJavalin(), (server, client) -> {
            // given: a structure
            projectAndItsCorrespondingStructureHaveBeenCreated(projectId, "Alice in Wonderland");
            String structureId = getStructureIdByProjectId(client, correctToken, projectId).getStructureId();
            StructureDTO structure = getStructure(client, correctToken, structureId).getStructure();

            // when: listening to structure events over the web socket
            var eventReceived = getLatchForAwaitingEventOverWebSocket(
                    client,
                    correctToken,
                    Structure.TYPE,
                    AggregateId.of(structureId),
                    StructureEvent.NODE_ADDED.getName()
            );

            // and: unsubscribing from the event
            eventReceived.unsubscribe();

            // and: adding a node
            addNodeChild(
                    client,
                    correctToken,
                    structureId,
                    structure.getVersion(),
                    structure.getRootNodeId(),
                    "Chapter 1"
            );

            // then: the event is not received
            assertThat(eventReceived.await(1, TimeUnit.SECONDS)).isFalse();
        });
    }

}
