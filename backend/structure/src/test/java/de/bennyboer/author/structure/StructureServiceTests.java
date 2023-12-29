package de.bennyboer.author.structure;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.author.eventsourcing.persistence.InMemoryEventSourcingRepo;
import de.bennyboer.author.eventsourcing.testing.TestEventPublisher;
import de.bennyboer.author.structure.nodes.Node;
import de.bennyboer.author.structure.nodes.NodeId;
import de.bennyboer.author.structure.nodes.NodeName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class StructureServiceTests {

    private final StructureService structureService = new StructureService(
            new InMemoryEventSourcingRepo(),
            new TestEventPublisher()
    );

    private final String testProjectId = "PROJECT_ID";
    private final Agent testAgent = Agent.user(UserId.of("TEST_USER_ID"));

    @Test
    void shouldCreateStructure() {
        // given: the name of the root node of the structure to be created
        var rootNodeName = NodeName.of("Alice in Wonderland");

        // when: a structure is created
        var structureIdAndVersion = structureService.create(testProjectId, rootNodeName, testAgent).block();
        var structureId = structureIdAndVersion.getId();
        var version = structureIdAndVersion.getVersion();

        // then: the structure can be retrieved
        var structure = structureService.get(structureId).block();
        var rootNode = structure.getRootNode();
        assertEquals(rootNodeName, rootNode.getName());
    }

    @Test
    void shouldRemoveStructure() {
        // given: a structure
        var rootNodeName = NodeName.of("Alice in Wonderland");
        var structureIdAndVersion = structureService.create(testProjectId, rootNodeName, testAgent).block();
        var structureId = structureIdAndVersion.getId();

        // when: the structure is removed
        structureService.remove(structureId, testAgent).block();

        // then: the structure cannot be retrieved
        var structure = structureService.get(structureId).block();
        assertNull(structure);
    }

    @Test
    void shouldNotBeAbleToDispatchCommandsAfterStructureIsRemoved() {
        // given: a deleted structure
        var rootNodeName = NodeName.of("Alice in Wonderland");
        var structureIdAndVersion = structureService.create(testProjectId, rootNodeName, testAgent).block();
        var structureId = structureIdAndVersion.getId();
        var version = structureService.remove(structureId, testAgent).block();

        // when: trying to add a node to the deleted structure
        Executable executable = () -> structureService.addNode(
                structureId,
                version,
                NodeId.create(),
                NodeName.of("Chapter 1"),
                testAgent
        ).block();

        // then: an exception is thrown
        var exception = assertThrows(
                IllegalStateException.class,
                executable
        );
        assertEquals("Cannot apply command to removed Structure", exception.getMessage());
    }

    @Test
    void shouldAddNode() {
        // given: a structure with a root node
        var rootNodeName = NodeName.of("Alice in Wonderland");
        var structureIdAndVersion = structureService.create(testProjectId, rootNodeName, testAgent).block();
        var structureId = structureIdAndVersion.getId();
        var version = structureIdAndVersion.getVersion();
        var initialStructure = structureService.get(structureId, version).block();

        // when: a node is added as child of the root node
        var newNodeName = NodeName.of("Chapter 1");
        var latestVersion = structureService.addNode(
                structureId,
                version,
                initialStructure.getRootNodeId(),
                newNodeName,
                testAgent
        ).block();

        // then: the root node has a child node
        var structure = structureService.get(structureId, latestVersion).block();
        var rootNode = structure.getRootNode();
        assertEquals(1, rootNode.getChildren().size());
        var childNodeId = rootNode.getChildren().get(0);
        var childNode = structure.getNodeById(childNodeId);
        assertEquals(Optional.of(newNodeName), childNode.map(Node::getName));
    }

    @Test
    void shouldRenameNode() {
        // given: a structure with a root node with a child
        var rootNodeName = NodeName.of("Alice in Wonderland");
        var structureIdAndVersion = structureService.create(testProjectId, rootNodeName, testAgent).block();
        var structureId = structureIdAndVersion.getId();
        var version = structureIdAndVersion.getVersion();
        var initialStructure = structureService.get(structureId, version).block();
        version = structureService.addNode(
                structureId,
                version,
                initialStructure.getRootNodeId(),
                NodeName.of("Chapter 1"),
                testAgent
        ).block();
        initialStructure = structureService.get(structureId, version).block();

        // when: the child node is renamed
        var newName = NodeName.of("Beginning");
        version = structureService.renameNode(
                structureId,
                version,
                initialStructure.getRootNode().getChildren().getFirst(),
                newName,
                testAgent
        ).block();

        // then: the child node has the new name
        var structure = structureService.get(structureId, version).block();
        var childNodeId = structure.getRootNode().getChildren().getFirst();
        var childNode = structure.getNodeById(childNodeId);
        assertEquals(Optional.of(newName), childNode.map(Node::getName));
    }

    @Test
    void shouldToggleNode() {
        // given: a structure with a root node with a child
        var rootNodeName = NodeName.of("Alice in Wonderland");
        var structureIdAndVersion = structureService.create(testProjectId, rootNodeName, testAgent).block();
        var structureId = structureIdAndVersion.getId();
        var version = structureIdAndVersion.getVersion();
        var initialStructure = structureService.get(structureId, version).block();
        version = structureService.addNode(
                structureId,
                version,
                initialStructure.getRootNodeId(),
                NodeName.of("Chapter 1"),
                testAgent
        ).block();

        // when: the root node is toggled
        version = structureService.toggleNode(
                structureId,
                version,
                initialStructure.getRootNodeId(),
                testAgent
        ).block();

        // then: the root node is collapsed
        var structure = structureService.get(structureId, version).block();
        var rootNode = structure.getRootNode();
        assertEquals(false, rootNode.isExpanded());

        // when: the root node is toggled again
        version = structureService.toggleNode(
                structureId,
                version,
                initialStructure.getRootNodeId(),
                testAgent
        ).block();

        // then: the root node is expanded
        structure = structureService.get(structureId, version).block();
        rootNode = structure.getRootNode();
        assertEquals(true, rootNode.isExpanded());
    }

    @Test
    void shouldRemoveLeafNode() {
        // given: a structure with a root node with a child
        var rootNodeName = NodeName.of("Alice in Wonderland");
        var structureIdAndVersion = structureService.create(testProjectId, rootNodeName, testAgent).block();
        var structureId = structureIdAndVersion.getId();
        var version = structureIdAndVersion.getVersion();
        var initialStructure = structureService.get(structureId, version).block();
        version = structureService.addNode(
                structureId,
                version,
                initialStructure.getRootNodeId(),
                NodeName.of("Chapter 1"),
                testAgent
        ).block();
        initialStructure = structureService.get(structureId, version).block();

        // when: the child node is removed
        version = structureService.removeNode(
                structureId,
                version,
                initialStructure.getRootNode().getChildren().getFirst(),
                testAgent
        ).block();

        // then: the root node has no children
        var structure = structureService.get(structureId, version).block();
        var rootNode = structure.getRootNode();
        assertEquals(0, rootNode.getChildren().size());
    }

    @Test
    void shouldNotAllowRemovingRootNode() {
        // given: a structure with a root node
        var rootNodeName = NodeName.of("Alice in Wonderland");
        var structureIdAndVersion = structureService.create(testProjectId, rootNodeName, testAgent).block();
        var structureId = structureIdAndVersion.getId();
        var version = structureIdAndVersion.getVersion();
        var initialStructure = structureService.get(structureId, version).block();

        // when: the root node is removed
        Executable executable = () -> structureService.removeNode(
                structureId,
                version,
                initialStructure.getRootNodeId(),
                testAgent
        ).block();

        // then: an exception is thrown
        var exception = assertThrows(
                IllegalArgumentException.class,
                executable
        );
        assertEquals("Cannot remove root node", exception.getMessage());
    }

    @Test
    void shouldRemoveParentNode() {
        // given: a structure with a root node with a child with a child
        var rootNodeName = NodeName.of("Alice in Wonderland");
        var structureIdAndVersion = structureService.create(testProjectId, rootNodeName, testAgent).block();
        var structureId = structureIdAndVersion.getId();
        var version = structureIdAndVersion.getVersion();
        var initialStructure = structureService.get(structureId, version).block();
        version = structureService.addNode(
                structureId,
                version,
                initialStructure.getRootNodeId(),
                NodeName.of("Chapter 1"),
                testAgent
        ).block();
        initialStructure = structureService.get(structureId, version).block();
        version = structureService.addNode(
                structureId,
                version,
                initialStructure.getRootNode().getChildren().getFirst(),
                NodeName.of("Chapter 1.1"),
                testAgent
        ).block();
        initialStructure = structureService.get(structureId, version).block();

        // when: the parent node is removed
        version = structureService.removeNode(
                structureId,
                version,
                initialStructure.getRootNode().getChildren().getFirst(),
                testAgent
        ).block();

        // then: the root node has no children
        var structure = structureService.get(structureId, version).block();
        var rootNode = structure.getRootNode();
        assertEquals(0, rootNode.getChildren().size());

        // and: there is only one node left
        assertEquals(1, structure.getNodes().size());
    }

    @Test
    void shouldSwapLeafNodes() {
        // given: a structure with a root node with two children
        var rootNodeName = NodeName.of("Alice in Wonderland");
        var structureIdAndVersion = structureService.create(testProjectId, rootNodeName, testAgent).block();
        var structureId = structureIdAndVersion.getId();
        var version = structureIdAndVersion.getVersion();
        var initialStructure = structureService.get(structureId, version).block();
        version = structureService.addNode(
                structureId,
                version,
                initialStructure.getRootNodeId(),
                NodeName.of("Chapter 1"),
                testAgent
        ).block();
        initialStructure = structureService.get(structureId, version).block();
        version = structureService.addNode(
                structureId,
                version,
                initialStructure.getRootNodeId(),
                NodeName.of("Chapter 2"),
                testAgent
        ).block();
        initialStructure = structureService.get(structureId, version).block();

        // when: the two child nodes are swapped
        version = structureService.swapNodes(
                structureId,
                version,
                initialStructure.getRootNode().getChildren().getFirst(),
                initialStructure.getRootNode().getChildren().getLast(),
                testAgent
        ).block();

        // then: the two child nodes are swapped
        var structure = structureService.get(structureId, version).block();
        var rootNode = structure.getRootNode();
        assertEquals(2, rootNode.getChildren().size());
        assertEquals(NodeName.of("Chapter 2"), structure.getNodeById(rootNode.getChildren().getFirst())
                .orElseThrow()
                .getName());
        assertEquals(NodeName.of("Chapter 1"), structure.getNodeById(rootNode.getChildren().getLast())
                .orElseThrow()
                .getName());
    }

    @Test
    void shouldSwapParentNodesThatAreNotDirectlyRelated() {
        // given: a structure with a root node with two children that themselves have children
        var rootNodeName = NodeName.of("Alice in Wonderland");
        var structureIdAndVersion = structureService.create(testProjectId, rootNodeName, testAgent).block();
        var structureId = structureIdAndVersion.getId();
        var version = structureIdAndVersion.getVersion();
        var initialStructure = structureService.get(structureId, version).block();
        version = structureService.addNode(
                structureId,
                version,
                initialStructure.getRootNodeId(),
                NodeName.of("Chapter 1"),
                testAgent
        ).block();
        initialStructure = structureService.get(structureId, version).block();
        version = structureService.addNode(
                structureId,
                version,
                initialStructure.getRootNodeId(),
                NodeName.of("Chapter 2"),
                testAgent
        ).block();
        initialStructure = structureService.get(structureId, version).block();
        version = structureService.addNode(
                structureId,
                version,
                initialStructure.getRootNode().getChildren().getFirst(),
                NodeName.of("Chapter 1.1"),
                testAgent
        ).block();
        initialStructure = structureService.get(structureId, version).block();
        version = structureService.addNode(
                structureId,
                version,
                initialStructure.getRootNode().getChildren().getLast(),
                NodeName.of("Chapter 2.1"),
                testAgent
        ).block();

        // when: the two parent nodes are swapped
        version = structureService.swapNodes(
                structureId,
                version,
                initialStructure.getRootNode().getChildren().getFirst(),
                initialStructure.getRootNode().getChildren().getLast(),
                testAgent
        ).block();

        // then: the two parent nodes are swapped
        var structure = structureService.get(structureId, version).block();
        var rootNode = structure.getRootNode();
        assertEquals(2, rootNode.getChildren().size());
        assertEquals(NodeName.of("Chapter 2"), structure.getNodeById(rootNode.getChildren().getFirst())
                .orElseThrow()
                .getName());
        assertEquals(NodeName.of("Chapter 1"), structure.getNodeById(rootNode.getChildren().getLast())
                .orElseThrow()
                .getName());
    }

    @Test
    void shouldNotBeAbleToSwapNodesThatAreDirectlyRelated() {
        // given: a structure with a root node with a child with a child
        var rootNodeName = NodeName.of("Alice in Wonderland");
        var structureIdAndVersion = structureService.create(testProjectId, rootNodeName, testAgent).block();
        var structureId = structureIdAndVersion.getId();
        var version = structureIdAndVersion.getVersion();
        var initialStructure = structureService.get(structureId, version).block();
        version = structureService.addNode(
                structureId,
                version,
                initialStructure.getRootNodeId(),
                NodeName.of("Chapter 1"),
                testAgent
        ).block();
        initialStructure = structureService.get(structureId, version).block();
        version = structureService.addNode(
                structureId,
                version,
                initialStructure.getRootNode().getChildren().getFirst(),
                NodeName.of("Chapter 1.1"),
                testAgent
        ).block();
        initialStructure = structureService.get(structureId, version).block();

        // when: the child node is swapped with the parent node
        var finalVersion = version;
        var parentNodeId = initialStructure.getRootNode().getChildren().getFirst();
        var parentNode = initialStructure.getNodeById(initialStructure.getRootNode().getChildren().getFirst())
                .orElseThrow();
        var childNodeId = parentNode.getChildren().getFirst();
        Executable executable = () -> structureService.swapNodes(
                structureId,
                finalVersion,
                parentNodeId,
                childNodeId,
                testAgent
        ).block();

        // then: an exception is thrown
        var exception = assertThrows(
                IllegalArgumentException.class,
                executable
        );
        assertEquals("Nodes are directly related", exception.getMessage());
    }

    @Test
    void shouldNotAcceptOtherCommandBeforeCreating() {
        // when: trying to add a node to a non-existing structure
        Executable executable = () -> structureService.addNode(
                StructureId.create(),
                Version.zero(),
                NodeId.create(),
                NodeName.of("Chapter 1"),
                testAgent
        ).block();

        // then: an exception is thrown
        var exception = assertThrows(
                IllegalStateException.class,
                executable
        );
        assertEquals(
                "Structure must be initialized with CreateCmd before applying other commands",
                exception.getMessage()
        );
    }

}
