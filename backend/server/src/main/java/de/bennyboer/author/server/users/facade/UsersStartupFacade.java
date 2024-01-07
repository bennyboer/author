package de.bennyboer.author.server.users.facade;

import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.author.server.users.persistence.lookup.UserLookupRepo;
import de.bennyboer.author.user.*;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Value
@AllArgsConstructor
public class UsersStartupFacade {

    UserService userService;

    UserLookupRepo userLookupRepo;

    public Mono<Void> initDefaultUserIfNecessary(
            String userName,
            String mail,
            String firstName,
            String lastName,
            String password
    ) {
        return userLookupRepo.countUsers()
                .filter(count -> count == 0)
                .flatMap(count -> createDefaultUser(
                        userName,
                        mail,
                        firstName,
                        lastName,
                        password
                ));
    }

    private Mono<Void> createDefaultUser(
            String userName,
            String mail,
            String firstName,
            String lastName,
            String password
    ) {
        // TODO Read config of the server to decide if default user should be created or not
        return userService
                .create(
                        UserName.of(userName),
                        Mail.of(mail),
                        FirstName.of(firstName),
                        LastName.of(lastName),
                        Password.of(password),
                        Agent.system()
                )
                .doOnNext((idAndVersion) -> log.info(
                        "Created default user with ID '{}', username '{}', mail '{}' and password '{}'",
                        idAndVersion.getId().getValue(),
                        userName,
                        mail,
                        password
                ))
                .then();
    }

}
