package de.bennyboer.author.server.project.rest;

import io.javalin.apibuilder.EndpointGroup;
import lombok.AllArgsConstructor;
import lombok.Value;

import static io.javalin.apibuilder.ApiBuilder.*;

@Value
@AllArgsConstructor
public class ProjectRestRouting implements EndpointGroup {

    ProjectRestHandler handler;

    @Override
    public void addEndpoints() {
        path("/{projectId}", () -> {
            get(handler::getProject);
            post(handler::createProject);
            post("/rename", handler::renameProject);
            delete(handler::removeProject);
        });
    }

}
