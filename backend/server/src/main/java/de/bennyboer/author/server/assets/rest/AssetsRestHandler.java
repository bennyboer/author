package de.bennyboer.author.server.assets.rest;

import de.bennyboer.author.server.assets.facade.AssetsCommandFacade;
import de.bennyboer.author.server.assets.facade.AssetsQueryFacade;
import io.javalin.http.Context;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class AssetsRestHandler {

    AssetsQueryFacade queryFacade;

    AssetsCommandFacade commandFacade;

    public void getAsset(Context ctx) {
        // TODO
    }

    public void createAsset(Context ctx) {
        // TODO
    }

    public void removeAsset(Context ctx) {
        // TODO
    }

    public void getAssetContent(Context ctx) {
        // TODO Load the asset and its content - write header content type to fit the asset type
        // TODO The frontend should be able to display the content and thus needs the proper content typeo
    }

}
