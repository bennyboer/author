package de.bennyboer.author.server.user.api;

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
public class UserDTO {

    String id;

    long version;

    String name;

    Instant createdAt;

}
