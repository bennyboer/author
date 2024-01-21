package de.bennyboer.author.server.assets.facade;

import de.bennyboer.author.assets.AssetsService;
import de.bennyboer.author.server.assets.permissions.AssetsPermissionsService;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AssetsQueryFacade {

    private final AssetsService assetsService;

    private final AssetsPermissionsService permissionsService;

    // TODO Fetch asset by ID

}
