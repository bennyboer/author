package de.bennyboer.author.server.projects.rest;

import de.bennyboer.author.server.projects.api.requests.CreateProjectRequest;
import de.bennyboer.author.server.projects.api.requests.RenameProjectRequest;
import de.bennyboer.author.server.projects.facade.ProjectsCommandFacade;
import de.bennyboer.author.server.projects.facade.ProjectsQueryFacade;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.server.shared.http.ReactiveHandler.handle;

@Value
@AllArgsConstructor
public class ProjectsRestHandler {

    ProjectsQueryFacade queryFacade;

    ProjectsCommandFacade commandFacade;

    public void getAccessibleProjects(Context ctx) {
        handle(ctx, (agent) -> queryFacade.getAccessibleProjects(agent).collectList(), ctx::json);
    }

    public void getProject(Context ctx) {
        var projectId = ctx.pathParam("projectId");

        handle(
                ctx,
                (agent) -> queryFacade.getProject(projectId, agent).singleOptional(),
                project -> project.ifPresentOrElse(
                        ctx::json,
                        () -> ctx.status(HttpStatus.NOT_FOUND)
                )
        );
    }

    public void createProject(Context ctx) {
        var request = ctx.bodyAsClass(CreateProjectRequest.class);

        handle(
                ctx,
                (agent) -> commandFacade.create(request.getName(), agent),
                res -> ctx.status(HttpStatus.NO_CONTENT)
                        .header("Location", "/api/projects/%s".formatted(res.getValue()))
        );
    }

    public void renameProject(Context ctx) {
        var request = ctx.bodyAsClass(RenameProjectRequest.class);
        var projectId = ctx.pathParam("projectId");
        var version = ctx.queryParamAsClass("version", Long.class).get();

        handle(
                ctx,
                (agent) -> commandFacade.rename(projectId, version, request.getName(), agent),
                res -> ctx.status(HttpStatus.NO_CONTENT)
        );
    }

    public void removeProject(Context ctx) {
        var projectId = ctx.pathParam("projectId");
        var version = ctx.queryParamAsClass("version", Long.class).get();

        handle(
                ctx,
                (agent) -> commandFacade.remove(projectId, version, agent),
                res -> ctx.status(HttpStatus.NO_CONTENT)
        );
    }

}
