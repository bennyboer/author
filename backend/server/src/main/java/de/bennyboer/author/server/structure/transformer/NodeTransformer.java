package de.bennyboer.author.server.structure.transformer;

import de.bennyboer.author.server.structure.api.NodeDTO;
import de.bennyboer.author.structure.tree.node.Node;
import de.bennyboer.author.structure.tree.node.NodeId;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NodeTransformer {

    public static Map<String, NodeDTO> toApi(Map<NodeId, Node> nodes) {
        return nodes.entrySet()
                .stream()
                .collect(Collectors.toMap(k -> k.getKey().getValue(), v -> toApi(v.getValue())));
    }

    public static NodeDTO toApi(Node node) {
        String name = node.getName().getValue();
        List<String> children = node.getChildren()
                .stream()
                .map(NodeId::getValue)
                .toList();
        boolean expanded = node.isExpanded();

        return NodeDTO.builder()
                .name(name)
                .children(children)
                .expanded(expanded)
                .build();
    }

}
