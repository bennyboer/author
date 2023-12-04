package de.bennyboer.author.structure;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.structure.tree.api.*;
import de.bennyboer.eventsourcing.api.Version;
import de.bennyboer.eventsourcing.api.persistence.InMemoryEventSourcingRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TreeServiceTests {

    private final TreeService treeService = new TreeService(new InMemoryEventSourcingRepo());

    private final UserId userId = UserId.of("TEST_USER_ID");

    @Test
    void shouldCreateTree() {
        // given: the name of the root node of the tree to be created
        var rootNodeName = NodeName.of("Alice in Wonderland");

        // when: a tree is created
        var treeIdAndVersion = treeService.create(rootNodeName, userId).block();
        var treeId = treeIdAndVersion.getId();
        var version = treeIdAndVersion.getVersion();

        // then: the tree can be retrieved
        var tree = treeService.get(treeId).block();
        var rootNode = tree.getRootNode();
        assertEquals(rootNodeName, rootNode.getName());
    }

    @Test
    void shouldAddNode() {
        // given: a tree with a root node
        var rootNodeName = NodeName.of("Alice in Wonderland");
        var treeIdAndVersion = treeService.create(rootNodeName, userId).block();
        var treeId = treeIdAndVersion.getId();
        var version = treeIdAndVersion.getVersion();
        var initialTree = treeService.get(treeId, version).block();

        // when: a node is added as child of the root node
        var newNodeName = NodeName.of("Chapter 1");
        var latestVersion = treeService.addNode(
                treeId,
                version,
                initialTree.getRootNodeId(),
                newNodeName,
                userId
        ).block();

        // then: the root node has a child node
        var tree = treeService.get(treeId, latestVersion).block();
        var rootNode = tree.getRootNode();
        assertEquals(1, rootNode.getChildren().size());
        var childNodeId = rootNode.getChildren().get(0);
        var childNode = tree.getNodeById(childNodeId);
        assertEquals(Optional.of(newNodeName), childNode.map(Node::getName));
    }

    @Test
    void shouldToggleNode() {
        // TODO Test that a node can be collapsed and expanded again
        // given: a tree with a root node with a child
        var rootNodeName = NodeName.of("Alice in Wonderland");
        var treeIdAndVersion = treeService.create(rootNodeName, userId).block();
        var treeId = treeIdAndVersion.getId();
        var version = treeIdAndVersion.getVersion();
        var initialTree = treeService.get(treeId, version).block();
        version = treeService.addNode(
                treeId,
                version,
                initialTree.getRootNodeId(),
                NodeName.of("Chapter 1"),
                userId
        ).block();

        // when: the root node is toggled
        version = treeService.toggleNode(
                treeId,
                version,
                initialTree.getRootNodeId(),
                userId
        ).block();

        // then: the root node is collapsed
        var tree = treeService.get(treeId, version).block();
        var rootNode = tree.getRootNode();
        assertEquals(false, rootNode.isExpanded());

        // when: the root node is toggled again
        version = treeService.toggleNode(
                treeId,
                version,
                initialTree.getRootNodeId(),
                userId
        ).block();

        // then: the root node is expanded
        tree = treeService.get(treeId, version).block();
        rootNode = tree.getRootNode();
        assertEquals(true, rootNode.isExpanded());
    }

    @Test
    void shouldRemoveLeafNode() {
        // given: a tree with a root node with a child
        var rootNodeName = NodeName.of("Alice in Wonderland");
        var treeIdAndVersion = treeService.create(rootNodeName, userId).block();
        var treeId = treeIdAndVersion.getId();
        var version = treeIdAndVersion.getVersion();
        var initialTree = treeService.get(treeId, version).block();
        version = treeService.addNode(
                treeId,
                version,
                initialTree.getRootNodeId(),
                NodeName.of("Chapter 1"),
                userId
        ).block();
        initialTree = treeService.get(treeId, version).block();

        // when: the child node is removed
        version = treeService.removeNode(
                treeId,
                version,
                initialTree.getRootNode().getChildren().getFirst(),
                userId
        ).block();

        // then: the root node has no children
        var tree = treeService.get(treeId, version).block();
        var rootNode = tree.getRootNode();
        assertEquals(0, rootNode.getChildren().size());
    }

    @Test
    void shouldNotAllowRemovingRootNode() {
        // given: a tree with a root node
        var rootNodeName = NodeName.of("Alice in Wonderland");
        var treeIdAndVersion = treeService.create(rootNodeName, userId).block();
        var treeId = treeIdAndVersion.getId();
        var version = treeIdAndVersion.getVersion();
        var initialTree = treeService.get(treeId, version).block();

        // when: the root node is removed
        Executable executable = () -> treeService.removeNode(
                treeId,
                version,
                initialTree.getRootNodeId(),
                userId
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
        // given: a tree with a root node with a child with a child
        var rootNodeName = NodeName.of("Alice in Wonderland");
        var treeIdAndVersion = treeService.create(rootNodeName, userId).block();
        var treeId = treeIdAndVersion.getId();
        var version = treeIdAndVersion.getVersion();
        var initialTree = treeService.get(treeId, version).block();
        version = treeService.addNode(
                treeId,
                version,
                initialTree.getRootNodeId(),
                NodeName.of("Chapter 1"),
                userId
        ).block();
        initialTree = treeService.get(treeId, version).block();
        version = treeService.addNode(
                treeId,
                version,
                initialTree.getRootNode().getChildren().getFirst(),
                NodeName.of("Chapter 1.1"),
                userId
        ).block();
        initialTree = treeService.get(treeId, version).block();

        // when: the parent node is removed
        version = treeService.removeNode(
                treeId,
                version,
                initialTree.getRootNode().getChildren().getFirst(),
                userId
        ).block();

        // then: the root node has no children
        var tree = treeService.get(treeId, version).block();
        var rootNode = tree.getRootNode();
        assertEquals(0, rootNode.getChildren().size());

        // and: there is only one node left
        assertEquals(1, tree.getNodes().size());
    }

    @Test
    void shouldSwapLeafNodes() {
        // given: a tree with a root node with two children
        var rootNodeName = NodeName.of("Alice in Wonderland");
        var treeIdAndVersion = treeService.create(rootNodeName, userId).block();
        var treeId = treeIdAndVersion.getId();
        var version = treeIdAndVersion.getVersion();
        var initialTree = treeService.get(treeId, version).block();
        version = treeService.addNode(
                treeId,
                version,
                initialTree.getRootNodeId(),
                NodeName.of("Chapter 1"),
                userId
        ).block();
        initialTree = treeService.get(treeId, version).block();
        version = treeService.addNode(
                treeId,
                version,
                initialTree.getRootNodeId(),
                NodeName.of("Chapter 2"),
                userId
        ).block();
        initialTree = treeService.get(treeId, version).block();

        // when: the two child nodes are swapped
        version = treeService.swapNodes(
                treeId,
                version,
                initialTree.getRootNode().getChildren().getFirst(),
                initialTree.getRootNode().getChildren().getLast(),
                userId
        ).block();

        // then: the two child nodes are swapped
        var tree = treeService.get(treeId, version).block();
        var rootNode = tree.getRootNode();
        assertEquals(2, rootNode.getChildren().size());
        assertEquals(NodeName.of("Chapter 2"), tree.getNodeById(rootNode.getChildren().getFirst())
                .orElseThrow()
                .getName());
        assertEquals(NodeName.of("Chapter 1"), tree.getNodeById(rootNode.getChildren().getLast())
                .orElseThrow()
                .getName());
    }

    @Test
    void shouldSwapParentNodesThatAreNotDirectlyRelated() {
        // given: a tree with a root node with two children that themselves have children
        var rootNodeName = NodeName.of("Alice in Wonderland");
        var treeIdAndVersion = treeService.create(rootNodeName, userId).block();
        var treeId = treeIdAndVersion.getId();
        var version = treeIdAndVersion.getVersion();
        var initialTree = treeService.get(treeId, version).block();
        version = treeService.addNode(
                treeId,
                version,
                initialTree.getRootNodeId(),
                NodeName.of("Chapter 1"),
                userId
        ).block();
        initialTree = treeService.get(treeId, version).block();
        version = treeService.addNode(
                treeId,
                version,
                initialTree.getRootNodeId(),
                NodeName.of("Chapter 2"),
                userId
        ).block();
        initialTree = treeService.get(treeId, version).block();
        version = treeService.addNode(
                treeId,
                version,
                initialTree.getRootNode().getChildren().getFirst(),
                NodeName.of("Chapter 1.1"),
                userId
        ).block();
        initialTree = treeService.get(treeId, version).block();
        version = treeService.addNode(
                treeId,
                version,
                initialTree.getRootNode().getChildren().getLast(),
                NodeName.of("Chapter 2.1"),
                userId
        ).block();

        // when: the two parent nodes are swapped
        version = treeService.swapNodes(
                treeId,
                version,
                initialTree.getRootNode().getChildren().getFirst(),
                initialTree.getRootNode().getChildren().getLast(),
                userId
        ).block();

        // then: the two parent nodes are swapped
        var tree = treeService.get(treeId, version).block();
        var rootNode = tree.getRootNode();
        assertEquals(2, rootNode.getChildren().size());
        assertEquals(NodeName.of("Chapter 2"), tree.getNodeById(rootNode.getChildren().getFirst())
                .orElseThrow()
                .getName());
        assertEquals(NodeName.of("Chapter 1"), tree.getNodeById(rootNode.getChildren().getLast())
                .orElseThrow()
                .getName());
    }

    @Test
    void shouldNotBeAbleToSwapNodesThatAreDirectlyRelated() {
        // given: a tree with a root node with a child with a child
        var rootNodeName = NodeName.of("Alice in Wonderland");
        var treeIdAndVersion = treeService.create(rootNodeName, userId).block();
        var treeId = treeIdAndVersion.getId();
        var version = treeIdAndVersion.getVersion();
        var initialTree = treeService.get(treeId, version).block();
        version = treeService.addNode(
                treeId,
                version,
                initialTree.getRootNodeId(),
                NodeName.of("Chapter 1"),
                userId
        ).block();
        initialTree = treeService.get(treeId, version).block();
        version = treeService.addNode(
                treeId,
                version,
                initialTree.getRootNode().getChildren().getFirst(),
                NodeName.of("Chapter 1.1"),
                userId
        ).block();
        initialTree = treeService.get(treeId, version).block();

        // when: the child node is swapped with the parent node
        var finalVersion = version;
        var parentNodeId = initialTree.getRootNode().getChildren().getFirst();
        var parentNode = initialTree.getNodeById(initialTree.getRootNode().getChildren().getFirst()).orElseThrow();
        var childNodeId = parentNode.getChildren().getFirst();
        Executable executable = () -> treeService.swapNodes(
                treeId,
                finalVersion,
                parentNodeId,
                childNodeId,
                userId
        ).block();

        // then: an exception is thrown
        var exception = assertThrows(
                IllegalArgumentException.class,
                executable
        );
        assertEquals("Cannot swap nodes that are directly related", exception.getMessage());
    }

    @Test
    void shouldNotAcceptOtherCommandBeforeCreating() {
        // when: trying to add a node to a non-existing tree
        Executable executable = () -> treeService.addNode(
                TreeId.create(),
                Version.zero(),
                NodeId.create(),
                NodeName.of("Chapter 1"),
                userId
        ).block();

        // then: an exception is thrown
        var exception = assertThrows(
                IllegalStateException.class,
                executable
        );
        assertEquals("Tree must be initialized with CreateCmd before applying other commands", exception.getMessage());
    }

}
