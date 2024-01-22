package de.bennyboer.author.server.assets.api.requests;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Jacksonized
public class CreateAssetRequest {

    String content;

    String contentType;

}
