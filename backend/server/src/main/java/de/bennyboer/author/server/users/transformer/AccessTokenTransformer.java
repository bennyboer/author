package de.bennyboer.author.server.users.transformer;

import de.bennyboer.author.server.users.api.AccessTokenDTO;
import de.bennyboer.author.user.AccessToken;

public class AccessTokenTransformer {

    public static AccessTokenDTO toApi(AccessToken accessToken) {
        String value = accessToken.getValue();

        return AccessTokenDTO.builder()
                .value(value)
                .build();
    }

}
