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

public class ToggleNodeTests extends StructureModuleTests {

    @Test
    void shouldToggleNode() {
        String projectId = "PROJECT_ID";

        JavalinTest.test(getJavalin(), (server, client) -> {
            // given: a structure
            projectAndItsCorrespondingStructureHaveBeenCreated(projectId, "Alice in Wonderland");
            String structureId = getStructureIdByProjectId(client, correctToken, projectId).getStructureId();
            StructureDTO structure = getStructure(client, correctToken, structureId).getStructure();

            // when: toggling a node
            int statusCode = toggleNode(
                    client,
                    correctToken,
                    structureId,
                    structure.getVersion(),
                    structure.getRootNodeId()
            );

            // then: the status code is 204
            assertThat(statusCode).isEqualTo(204);

            // and: the structure has been updated
            StructureDTO updatedStructure = getStructure(client, correctToken, structureId).getStructure();
            NodeDTO rootNode = updatedStructure.getNodes().get(structure.getRootNodeId());
            assertThat(rootNode.isExpanded()).isFalse();
        });
    }

    @Test
    void shouldNotBeAbleToToggleNodeGivenAnIncorrectToken() {
        String projectId = "PROJECT_ID";

        JavalinTest.test(getJavalin(), (server, client) -> {
            // given: a structure
            projectAndItsCorrespondingStructureHaveBeenCreated(projectId, "Alice in Wonderland");
            String structureId = getStructureIdByProjectId(client, correctToken, projectId).getStructureId();
            StructureDTO structure = getStructure(client, correctToken, structureId).getStructure();

            // when: toggling a node with an incorrect token
            int statusCode = toggleNode(
                    client,
                    incorrectToken,
                    structureId,
                    structure.getVersion(),
                    structure.getRootNodeId()
            );

            // then: the status code is 401
            assertThat(statusCode).isEqualTo(401);

            // and: the structure has not been updated
            StructureDTO updatedStructure = getStructure(client, correctToken, structureId).getStructure();
            NodeDTO rootNode = updatedStructure.getNodes().get(structure.getRootNodeId());
            assertThat(rootNode.isExpanded()).isTrue();
        });
    }

    @Test
    void shouldReceiveEventOverWebSocketWhenNodeIsToggled() {
        String projectId = "PROJECT_ID";

        JavalinTest.test(getJavalin(), (server, client) -> {
            // given: a structure
            projectAndItsCorrespondingStructureHaveBeenCreated(projectId, "Alice in Wonderland");
            String structureId = getStructureIdByProjectId(client, correctToken, projectId).getStructureId();
            StructureDTO structure = getStructure(client, correctToken, structureId).getStructure();

            // and: listening to events over web socket
            var eventReceived = getLatchForAwaitingEventOverWebSocket(
                    client,
                    correctToken,
                    Structure.TYPE,
                    AggregateId.of(structureId),
                    StructureEvent.NODE_TOGGLED.getName()
            );

            // when: toggling a node
            toggleNode(
                    client,
                    correctToken,
                    structureId,
                    structure.getVersion(),
                    structure.getRootNodeId()
            );

            // then: the event is received
            assertThat(eventReceived.await(5, TimeUnit.SECONDS)).isTrue();
        });
    }

}
