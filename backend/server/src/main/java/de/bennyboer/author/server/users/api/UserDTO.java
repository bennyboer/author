package de.bennyboer.author.server.users.api;

import jakarta.annotation.Nullable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;
import java.util.Optional;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Jacksonized
public class UserDTO {

    String id;

    long version;

    String name;

    String mail;

    String firstName;

    String lastName;

    @Nullable
    String imageId;

    Instant createdAt;

    public Optional<String> getImageId() {
        return Optional.ofNullable(imageId);
    }

}
