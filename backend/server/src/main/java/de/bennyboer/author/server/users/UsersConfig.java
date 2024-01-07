package de.bennyboer.author.server.users;

import de.bennyboer.author.auth.token.TokenGenerator;
import de.bennyboer.author.eventsourcing.persistence.EventSourcingRepo;
import de.bennyboer.author.permissions.repo.PermissionsRepo;
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
    DefaultUserDetails defaultUserDetails = DefaultUserDetails.of(
            "default",
            "default+test@example.com",
            "John",
            "Doe",
            "password"
    );

    EventSourcingRepo eventSourcingRepo;

    PermissionsRepo permissionsRepo;

    UserLookupRepo userLookupRepo;

    @Value
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class DefaultUserDetails {

        String username;

        String mail;

        String firstName;

        String lastName;

        String password;

        public static DefaultUserDetails of(
                String username,
                String mail,
                String firstName,
                String lastName,
                String password
        ) {
            checkNotNull(username, "Default user username must be given");
            checkNotNull(mail, "Default user mail must be given");
            checkNotNull(firstName, "Default user first name must be given");
            checkNotNull(lastName, "Default user last name must be given");
            checkNotNull(password, "Default user password must be given");

            return new DefaultUserDetails(username, mail, firstName, lastName, password);
        }

    }

}
