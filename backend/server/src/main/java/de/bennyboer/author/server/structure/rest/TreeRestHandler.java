package de.bennyboer.author.server.structure.rest;

import de.bennyboer.author.server.structure.api.requests.AddChildRequest;
import de.bennyboer.author.server.structure.api.requests.RenameNodeRequest;
import de.bennyboer.author.server.structure.api.requests.SwapNodesRequest;
import de.bennyboer.author.server.structure.facade.TreeCommandFacade;
import de.bennyboer.author.server.structure.facade.TreeQueryFacade;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.server.shared.http.ReactiveHandler.handle;

@Value
@AllArgsConstructor
public class TreeRestHandler {

    TreeQueryFacade queryFacade;

    TreeCommandFacade commandFacade;

    public void findTreeByProjectId(Context ctx) {
        var projectId = ctx.pathParam("projectId");

        handle(
                ctx,
                (agent) -> queryFacade.findTreeIdByProjectId(projectId, agent).singleOptional(),
                tree -> tree.ifPresentOrElse(
                        ctx::json,
                        () -> ctx.status(HttpStatus.NOT_FOUND)
                )
        );
    }

    public void getTree(Context ctx) {
        var treeId = ctx.pathParam("treeId");

        handle(ctx, (agent) -> queryFacade.getTree(treeId, agent).singleOptional(), tree -> tree.ifPresentOrElse(
                ctx::json,
                () -> ctx.status(HttpStatus.NOT_FOUND)
        ));
    }

    public void toggleNode(Context ctx) {
        var treeId = ctx.pathParam("treeId");
        var treeVersion = ctx.queryParamAsClass("version", Long.class).get();
        var nodeId = ctx.pathParam("nodeId");

        handle(
                ctx,
                (agent) -> commandFacade.toggleNode(treeId, treeVersion, nodeId, agent),
                tree -> ctx.status(HttpStatus.NO_CONTENT)
        );
    }

    public void addChild(Context ctx) {
        var treeId = ctx.pathParam("treeId");
        var treeVersion = ctx.queryParamAsClass("version", Long.class).get();
        var parentNodeId = ctx.pathParam("nodeId");
        var request = ctx.bodyAsClass(AddChildRequest.class);

        handle(
                ctx,
                (agent) -> commandFacade.addNode(treeId, treeVersion, parentNodeId, request.getName(), agent),
                tree -> ctx.status(HttpStatus.NO_CONTENT)
        );
    }

    public void renameNode(Context ctx) {
        var treeId = ctx.pathParam("treeId");
        var treeVersion = ctx.queryParamAsClass("version", Long.class).get();
        var nodeId = ctx.pathParam("nodeId");
        var request = ctx.bodyAsClass(RenameNodeRequest.class);

        handle(
                ctx,
                (agent) -> commandFacade.renameNode(treeId, treeVersion, nodeId, request.getName(), agent),
                tree -> ctx.status(HttpStatus.NO_CONTENT)
        );
    }

    public void removeNode(Context ctx) {
        var treeId = ctx.pathParam("treeId");
        var treeVersion = ctx.queryParamAsClass("version", Long.class).get();
        var nodeId = ctx.pathParam("nodeId");

        handle(
                ctx,
                (agent) -> commandFacade.removeNode(treeId, treeVersion, nodeId, agent),
                tree -> ctx.status(HttpStatus.NO_CONTENT)
        );
    }

    public void swapNodes(Context ctx) {
        var treeId = ctx.pathParam("treeId");
        var treeVersion = ctx.queryParamAsClass("version", Long.class).get();
        var request = ctx.bodyAsClass(SwapNodesRequest.class);

        handle(
                ctx,
                (agent) -> commandFacade.swapNodes(
                        treeId,
                        treeVersion,
                        request.getNodeId1(),
                        request.getNodeId2(),
                        agent
                ),
                tree -> ctx.status(HttpStatus.NO_CONTENT)
        );
    }

}
