package de.bennyboer.author.server.structure.rest;

import de.bennyboer.author.server.shared.http.Auth;
import de.bennyboer.author.server.structure.api.requests.AddChildRequest;
import de.bennyboer.author.server.structure.api.requests.RenameNodeRequest;
import de.bennyboer.author.server.structure.api.requests.SwapNodesRequest;
import de.bennyboer.author.server.structure.facade.TreeFacade;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class TreeRestHandler {

    TreeFacade facade;

    public void getTree(Context ctx) {
        var treeId = ctx.pathParam("treeId");

        ctx.future(() -> Auth.toAgent(ctx)
                .flatMap(agent -> facade.getTree(treeId, agent))
                .singleOptional()
                .toFuture()
                .thenAccept(tree -> tree.ifPresentOrElse(
                        ctx::json,
                        () -> ctx.status(HttpStatus.NOT_FOUND)
                )));
    }

    public void toggleNode(Context ctx) {
        var treeId = ctx.pathParam("treeId");
        var treeVersion = ctx.queryParamAsClass("version", Long.class).get();
        var nodeId = ctx.pathParam("nodeId");

        ctx.future(() -> Auth.toAgent(ctx)
                .flatMap(agent -> facade.toggleNode(treeId, treeVersion, nodeId, agent))
                .toFuture()
                .thenRun(() -> ctx.status(HttpStatus.NO_CONTENT)));
    }

    public void addChild(Context ctx) {
        var treeId = ctx.pathParam("treeId");
        var treeVersion = ctx.queryParamAsClass("version", Long.class).get();
        var parentNodeId = ctx.pathParam("nodeId");
        var request = ctx.bodyAsClass(AddChildRequest.class);

        ctx.future(() -> Auth.toAgent(ctx)
                .flatMap(agent -> facade.addNode(treeId, treeVersion, parentNodeId, request.getName(), agent))
                .toFuture()
                .thenRun(() -> ctx.status(HttpStatus.NO_CONTENT)));
    }

    public void renameNode(Context ctx) {
        var treeId = ctx.pathParam("treeId");
        var treeVersion = ctx.queryParamAsClass("version", Long.class).get();
        var nodeId = ctx.pathParam("nodeId");
        var request = ctx.bodyAsClass(RenameNodeRequest.class);

        ctx.future(() -> Auth.toAgent(ctx)
                .flatMap(agent -> facade.renameNode(treeId, treeVersion, nodeId, request.getName(), agent))
                .toFuture()
                .thenRun(() -> ctx.status(HttpStatus.NO_CONTENT)));
    }

    public void removeNode(Context ctx) {
        var treeId = ctx.pathParam("treeId");
        var treeVersion = ctx.queryParamAsClass("version", Long.class).get();
        var nodeId = ctx.pathParam("nodeId");

        ctx.future(() -> Auth.toAgent(ctx)
                .flatMap(agent -> facade.removeNode(treeId, treeVersion, nodeId, agent))
                .toFuture()
                .thenRun(() -> ctx.status(HttpStatus.NO_CONTENT)));
    }

    public void swapNodes(Context ctx) {
        var treeId = ctx.pathParam("treeId");
        var treeVersion = ctx.queryParamAsClass("version", Long.class).get();
        var request = ctx.bodyAsClass(SwapNodesRequest.class);

        ctx.future(() -> Auth.toAgent(ctx)
                .flatMap(agent -> facade.swapNodes(
                        treeId,
                        treeVersion,
                        request.getNodeId1(),
                        request.getNodeId2(),
                        agent
                ))
                .toFuture()
                .thenRun(() -> ctx.status(HttpStatus.NO_CONTENT)));
    }

}
