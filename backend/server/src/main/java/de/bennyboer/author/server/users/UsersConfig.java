package de.bennyboer.author.server.users;

import de.bennyboer.author.auth.token.TokenGenerator;
import de.bennyboer.author.server.users.persistence.lookup.UserLookupRepo;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class UsersConfig {

    TokenGenerator tokenGenerator;

    @Builder.Default
    DefaultUserCredentials defaultUserCredentials = DefaultUserCredentials.of("default", "password");

    UserLookupRepo userLookupRepo;

    @Value
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class DefaultUserCredentials {

        String username;

        String password;

        public static DefaultUserCredentials of(String username, String password) {
            checkNotNull(username, "Default user username must be given");
            checkNotNull(password, "Default user password must be given");

            return new DefaultUserCredentials(username, password);
        }

    }

}
