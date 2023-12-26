package de.bennyboer.author.server.projects.rest;

import de.bennyboer.author.server.projects.api.requests.CreateProjectRequest;
import de.bennyboer.author.server.projects.api.requests.RenameProjectRequest;
import de.bennyboer.author.server.projects.facade.ProjectsCommandFacade;
import de.bennyboer.author.server.projects.facade.ProjectsQueryFacade;
import de.bennyboer.author.server.shared.http.Auth;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class ProjectsRestHandler {

    ProjectsQueryFacade queryFacade;

    ProjectsCommandFacade commandFacade;

    public void getAccessibleProjects(Context ctx) {
        ctx.future(() -> Auth.toAgent(ctx)
                .flatMapMany(queryFacade::getAccessibleProjects)
                .collectList()
                .toFuture()
                .thenAccept(ctx::json));
    }

    public void getProject(Context ctx) {
        var projectId = ctx.pathParam("projectId");

        ctx.future(() -> Auth.toAgent(ctx)
                .flatMap(agent -> queryFacade.getProject(projectId, agent))
                .singleOptional()
                .toFuture()
                .thenAccept(tree -> tree.ifPresentOrElse(
                        ctx::json,
                        () -> ctx.status(HttpStatus.NOT_FOUND)
                )));
    }

    public void createProject(Context ctx) {
        var request = ctx.bodyAsClass(CreateProjectRequest.class);

        ctx.future(() -> Auth.toAgent(ctx)
                .flatMap(agent -> commandFacade.create(request.getName(), agent))
                .toFuture()
                .thenRun(() -> ctx.status(HttpStatus.NO_CONTENT)));
    }

    public void renameProject(Context ctx) {
        var request = ctx.bodyAsClass(RenameProjectRequest.class);
        var projectId = ctx.pathParam("projectId");
        var version = ctx.queryParamAsClass("version", Long.class).get();

        ctx.future(() -> Auth.toAgent(ctx)
                .flatMap(agent -> commandFacade.rename(projectId, version, request.getName(), agent))
                .toFuture()
                .thenRun(() -> ctx.status(HttpStatus.NO_CONTENT)));
    }

    public void removeProject(Context ctx) {
        var projectId = ctx.pathParam("projectId");
        var version = ctx.queryParamAsClass("version", Long.class).get();

        ctx.future(() -> Auth.toAgent(ctx)
                .flatMap(agent -> commandFacade.remove(projectId, version, agent))
                .toFuture()
                .thenRun(() -> ctx.status(HttpStatus.NO_CONTENT)));
    }

}
