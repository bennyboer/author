package de.bennyboer.author.server.assets.rest;

import de.bennyboer.author.server.assets.api.requests.CreateAssetRequest;
import de.bennyboer.author.server.assets.facade.AssetsCommandFacade;
import de.bennyboer.author.server.assets.facade.AssetsQueryFacade;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.server.shared.http.ReactiveHandler.handle;

@Value
@AllArgsConstructor
public class AssetsRestHandler {

    AssetsQueryFacade queryFacade;

    AssetsCommandFacade commandFacade;

    public void getAsset(Context ctx) {
        var assetId = ctx.pathParam("assetId");

        handle(
                ctx,
                (agent) -> queryFacade.getAsset(assetId, agent).singleOptional(),
                asset -> asset.ifPresentOrElse(
                        ctx::json,
                        () -> ctx.status(HttpStatus.NOT_FOUND)
                )
        );
    }

    public void createAsset(Context ctx) {
        var request = ctx.bodyAsClass(CreateAssetRequest.class);

        handle(
                ctx,
                (agent) -> commandFacade.create(request.getContent(), request.getContentType(), agent),
                res -> ctx.status(HttpStatus.NO_CONTENT)
                        .header("Location", "/api/assets/%s".formatted(res.getValue()))
        );
    }

    public void removeAsset(Context ctx) {
        var assetId = ctx.pathParam("assetId");
        var version = ctx.queryParamAsClass("version", Long.class).get();

        handle(
                ctx,
                (agent) -> commandFacade.remove(assetId, version, agent),
                res -> ctx.status(HttpStatus.NO_CONTENT)
        );
    }

    public void getAssetContent(Context ctx) {
        var assetId = ctx.pathParam("assetId");

        handle(
                ctx,
                (agent) -> queryFacade.getAsset(assetId, agent).singleOptional(),
                asset -> asset.ifPresentOrElse(
                        a -> {
                            ctx.contentType(a.getContentType());
                            ctx.result(a.getContent());
                        },
                        () -> ctx.status(HttpStatus.NOT_FOUND)
                )
        );
    }

}
