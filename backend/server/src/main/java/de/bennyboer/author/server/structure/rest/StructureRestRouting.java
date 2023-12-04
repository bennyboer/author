package de.bennyboer.author.server.structure.rest;

import io.javalin.apibuilder.EndpointGroup;
import lombok.AllArgsConstructor;
import lombok.Value;

import static io.javalin.apibuilder.ApiBuilder.*;

@Value
@AllArgsConstructor
public class StructureRestRouting implements EndpointGroup {

    TreeRestHandler treeHandler;

    @Override
    public void addEndpoints() {
        path("/trees", () -> path("/{treeId}", () -> {
            get(treeHandler::getTree);
            path("/nodes", () -> {
                post("/swap", treeHandler::swapNodes);
                path("/{nodeId}", () -> {
                    post("/toggle", treeHandler::toggleNode);
                    post("/add-child", treeHandler::addChild);
                    delete(treeHandler::removeNode);
                });
            });
        }));
    }

}