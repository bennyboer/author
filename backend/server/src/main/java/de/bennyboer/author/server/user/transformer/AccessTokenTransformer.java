package de.bennyboer.author.server.user.transformer;

import de.bennyboer.author.server.user.api.AccessTokenDTO;
import de.bennyboer.author.user.AccessToken;

import java.time.Instant;

public class AccessTokenTransformer {

    public static AccessTokenDTO toApi(AccessToken accessToken) {
        String value = accessToken.getValue();
        Instant expiresAt = accessToken.getExpiresAt();

        return AccessTokenDTO.builder()
                .value(value)
                .expiresAt(expiresAt)
                .build();
    }

}
