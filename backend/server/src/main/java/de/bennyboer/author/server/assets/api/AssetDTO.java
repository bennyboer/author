package de.bennyboer.author.server.assets.api;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Jacksonized
public class AssetDTO {

    String id;

    long version;

    String content;

    String contentType;

    Instant createdAt;

}
