package de.bennyboer.author.server.user.transformer;

import de.bennyboer.author.server.user.api.AccessTokenDTO;
import de.bennyboer.author.user.AccessToken;

public class AccessTokenTransformer {

    public static AccessTokenDTO toApi(AccessToken accessToken) {
        String value = accessToken.getValue();

        return AccessTokenDTO.builder()
                .value(value)
                .build();
    }

}
