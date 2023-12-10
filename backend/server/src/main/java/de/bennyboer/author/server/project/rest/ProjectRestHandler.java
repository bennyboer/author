package de.bennyboer.author.server.project.rest;

import de.bennyboer.author.server.project.api.requests.CreateProjectRequest;
import de.bennyboer.author.server.project.api.requests.RenameProjectRequest;
import de.bennyboer.author.server.project.facade.ProjectFacade;
import de.bennyboer.common.UserId;
import de.bennyboer.eventsourcing.event.metadata.agent.Agent;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class ProjectRestHandler {

    ProjectFacade facade;

    public void getProject(Context ctx) {
        var projectId = ctx.pathParam("projectId");

        ctx.future(() -> facade.getProject(projectId)
                .singleOptional()
                .toFuture()
                .thenAccept(tree -> tree.ifPresentOrElse(
                        ctx::json,
                        () -> ctx.status(HttpStatus.NOT_FOUND)
                )));
    }

    public void createProject(Context ctx) {
        var request = ctx.bodyAsClass(CreateProjectRequest.class);
        var agent = Agent.user(UserId.of("TEST_USER_ID")); // TODO Get agent from authentication

        ctx.future(() -> facade.create(request.getName(), agent)
                .toFuture()
                .thenRun(() -> ctx.status(HttpStatus.NO_CONTENT)));
    }

    public void renameProject(Context ctx) {
        var request = ctx.bodyAsClass(RenameProjectRequest.class);
        var projectId = ctx.pathParam("projectId");
        var version = ctx.queryParamAsClass("version", Long.class).get();
        var agent = Agent.user(UserId.of("TEST_USER_ID")); // TODO Get agent from authentication

        ctx.future(() -> facade.rename(projectId, version, request.getName(), agent)
                .toFuture()
                .thenRun(() -> ctx.status(HttpStatus.NO_CONTENT)));
    }

    public void removeProject(Context ctx) {
        var projectId = ctx.pathParam("projectId");
        var version = ctx.queryParamAsClass("version", Long.class).get();
        var agent = Agent.user(UserId.of("TEST_USER_ID")); // TODO Get agent from authentication

        ctx.future(() -> facade.remove(projectId, version, agent)
                .toFuture()
                .thenRun(() -> ctx.status(HttpStatus.NO_CONTENT)));
    }

}
