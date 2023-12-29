package de.bennyboer.author.server.structure.rest;

import de.bennyboer.author.server.structure.api.requests.AddChildRequest;
import de.bennyboer.author.server.structure.api.requests.RenameNodeRequest;
import de.bennyboer.author.server.structure.api.requests.SwapNodesRequest;
import de.bennyboer.author.server.structure.facade.StructureCommandFacade;
import de.bennyboer.author.server.structure.facade.StructureQueryFacade;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.server.shared.http.ReactiveHandler.handle;

@Value
@AllArgsConstructor
public class StructureRestHandler {

    StructureQueryFacade queryFacade;

    StructureCommandFacade commandFacade;

    public void findStructureByProjectId(Context ctx) {
        var projectId = ctx.pathParam("projectId");

        handle(
                ctx,
                (agent) -> queryFacade.findStructureIdByProjectId(projectId, agent).singleOptional(),
                structure -> structure.ifPresentOrElse(
                        ctx::json,
                        () -> ctx.status(HttpStatus.NOT_FOUND)
                )
        );
    }

    public void getStructure(Context ctx) {
        var structureId = ctx.pathParam("structureId");

        handle(
                ctx,
                (agent) -> queryFacade.getStructure(structureId, agent).singleOptional(),
                structure -> structure.ifPresentOrElse(
                        ctx::json,
                        () -> ctx.status(HttpStatus.NOT_FOUND)
                )
        );
    }

    public void toggleNode(Context ctx) {
        var structureId = ctx.pathParam("structureId");
        var version = ctx.queryParamAsClass("version", Long.class).get();
        var nodeId = ctx.pathParam("nodeId");

        handle(
                ctx,
                (agent) -> commandFacade.toggleNode(structureId, version, nodeId, agent),
                structure -> ctx.status(HttpStatus.NO_CONTENT)
        );
    }

    public void addChild(Context ctx) {
        var structureId = ctx.pathParam("structureId");
        var version = ctx.queryParamAsClass("version", Long.class).get();
        var parentNodeId = ctx.pathParam("nodeId");
        var request = ctx.bodyAsClass(AddChildRequest.class);

        handle(
                ctx,
                (agent) -> commandFacade.addNode(structureId, version, parentNodeId, request.getName(), agent),
                structure -> ctx.status(HttpStatus.NO_CONTENT)
        );
    }

    public void renameNode(Context ctx) {
        var structureId = ctx.pathParam("structureId");
        var version = ctx.queryParamAsClass("version", Long.class).get();
        var nodeId = ctx.pathParam("nodeId");
        var request = ctx.bodyAsClass(RenameNodeRequest.class);

        handle(
                ctx,
                (agent) -> commandFacade.renameNode(structureId, version, nodeId, request.getName(), agent),
                structure -> ctx.status(HttpStatus.NO_CONTENT)
        );
    }

    public void removeNode(Context ctx) {
        var structureId = ctx.pathParam("structureId");
        var version = ctx.queryParamAsClass("version", Long.class).get();
        var nodeId = ctx.pathParam("nodeId");

        handle(
                ctx,
                (agent) -> commandFacade.removeNode(structureId, version, nodeId, agent),
                structure -> ctx.status(HttpStatus.NO_CONTENT)
        );
    }

    public void swapNodes(Context ctx) {
        var structureId = ctx.pathParam("structureId");
        var version = ctx.queryParamAsClass("version", Long.class).get();
        var request = ctx.bodyAsClass(SwapNodesRequest.class);

        handle(
                ctx,
                (agent) -> commandFacade.swapNodes(
                        structureId,
                        version,
                        request.getNodeId1(),
                        request.getNodeId2(),
                        agent
                ),
                structure -> ctx.status(HttpStatus.NO_CONTENT)
        );
    }

}
