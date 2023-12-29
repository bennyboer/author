package de.bennyboer.author.server.structure.rest;

import io.javalin.apibuilder.EndpointGroup;
import lombok.AllArgsConstructor;
import lombok.Value;

import static io.javalin.apibuilder.ApiBuilder.*;

@Value
@AllArgsConstructor
public class StructureRestRouting implements EndpointGroup {

    StructureRestHandler handler;

    @Override
    public void addEndpoints() {
        get("/by-project-id/{projectId}", handler::findStructureByProjectId);
        path("/{structureId}", () -> {
            get(handler::getStructure);
            path("/nodes", () -> {
                post("/swap", handler::swapNodes);
                path("/{nodeId}", () -> {
                    post("/rename", handler::renameNode);
                    post("/toggle", handler::toggleNode);
                    post("/add-child", handler::addChild);
                    delete(handler::removeNode);
                });
            });
        });
    }

}
