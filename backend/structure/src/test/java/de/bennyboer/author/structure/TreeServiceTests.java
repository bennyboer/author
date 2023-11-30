package de.bennyboer.author.structure;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.structure.tree.TreeService;
import de.bennyboer.author.structure.tree.node.NodeName;
import de.bennyboer.eventsourcing.api.persistence.InMemoryEventSourcingRepo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TreeServiceTests {

    private final TreeService treeService = new TreeService(new InMemoryEventSourcingRepo());

    private final UserId userId = UserId.of("TEST_USER_ID");

    @Test
    void shouldCreateTree() {
        // given: the name of the root node of the tree to be created
        var rootNodeName = NodeName.of("Alice in Wonderland");

        // when: a tree is created
        var treeId = treeService.create(rootNodeName, userId).block();

        // then: the tree can be retrieved
        var tree = treeService.get(treeId).block();
        var rootNode = tree.getRootNode();
        assertEquals(rootNodeName, rootNode.getName());
    }

    @Test
    void shouldAddNode() {
        // TODO
    }

    @Test
    void shouldToggleNode() {
        // TODO Test that a node can be collapsed and expanded again
    }

    @Test
    void shouldRemoveLeafNode() {
        // TODO
    }

    @Test
    void shouldRemoveParentNode() {
        // TODO Test that all children are removed as well
    }

    @Test
    void shouldSwapLeafNodes() {
        // TODO
    }

    @Test
    void shouldSwapParentNodesThatAreNotDirectlyRelated() {
        // TODO Test that two parent nodes can be swapped that are not directly related (not parent or child of one
        //  another)
    }

    @Test
    void shouldNotBeAbleToSwapNodesThatAreDirectlyRelated() {
        // TODO Test that two nodes can be swapped that are directly related (parent or child of one another)
    }

}
