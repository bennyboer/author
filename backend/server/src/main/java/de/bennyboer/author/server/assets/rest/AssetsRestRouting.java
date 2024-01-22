package de.bennyboer.author.server.assets.rest;

import io.javalin.apibuilder.EndpointGroup;
import lombok.AllArgsConstructor;
import lombok.Value;

import static io.javalin.apibuilder.ApiBuilder.*;

@Value
@AllArgsConstructor
public class AssetsRestRouting implements EndpointGroup {

    AssetsRestHandler handler;

    @Override
    public void addEndpoints() {
        post(handler::createAsset);
        path("/{assetId}", () -> {
            get(handler::getAsset);
            get("/content", handler::getAssetContent);
            delete(handler::removeAsset);
        });
    }

}
