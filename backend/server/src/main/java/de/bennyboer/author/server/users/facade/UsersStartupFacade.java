package de.bennyboer.author.server.users.facade;

import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.author.server.users.persistence.lookup.UserLookupRepo;
import de.bennyboer.author.user.Password;
import de.bennyboer.author.user.UserName;
import de.bennyboer.author.user.UserService;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Value
@AllArgsConstructor
public class UsersStartupFacade {

    private static final UserName DEFAULT_USER_NAME = UserName.of("default");

    private static final Password DEFAULT_USER_PASSWORD = Password.of("password");

    UserService userService;

    UserLookupRepo userLookupRepo;

    public Mono<Void> initDefaultUserIfNecessary() {
        return userLookupRepo.countUsers()
                .filter(count -> count == 0)
                .flatMap(count -> createDefaultUser());
    }

    private Mono<Void> createDefaultUser() {
        // TODO Read config of the server to decide if default user should be created or not
        return userService
                .create(DEFAULT_USER_NAME, DEFAULT_USER_PASSWORD, Agent.system())
                .doOnNext((idAndVersion) -> log.warn(
                        "Created default user with ID '{}', username '{}' and password '{}'",
                        idAndVersion.getId().getValue(),
                        DEFAULT_USER_NAME.getValue(),
                        DEFAULT_USER_PASSWORD.getValue()
                ))
                .then();
    }

}
