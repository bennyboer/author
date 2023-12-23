package de.bennyboer.author.server.projects.rest;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.server.projects.api.requests.CreateProjectRequest;
import de.bennyboer.author.server.projects.api.requests.RenameProjectRequest;
import de.bennyboer.author.server.projects.facade.ProjectsFacade;
import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class ProjectsRestHandler {

    ProjectsFacade facade;

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
