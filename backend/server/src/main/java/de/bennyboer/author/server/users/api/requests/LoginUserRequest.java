package de.bennyboer.author.server.users.api.requests;

import jakarta.annotation.Nullable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.Optional;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Jacksonized
public class LoginUserRequest {

    @Nullable
    String name;

    @Nullable
    String mail;

    String password;

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    public Optional<String> getMail() {
        return Optional.ofNullable(mail);
    }

}
