package de.bennyboer.author.server.user.facade;

import de.bennyboer.author.server.user.api.AccessTokenDTO;
import de.bennyboer.author.server.user.api.UserDTO;
import de.bennyboer.author.server.user.persistence.lookup.UserLookupRepo;
import de.bennyboer.author.server.user.transformer.AccessTokenTransformer;
import de.bennyboer.author.server.user.transformer.UserTransformer;
import de.bennyboer.author.user.Password;
import de.bennyboer.author.user.UserName;
import de.bennyboer.author.user.UserService;
import de.bennyboer.common.UserId;
import de.bennyboer.eventsourcing.Version;
import de.bennyboer.eventsourcing.event.metadata.agent.Agent;
import lombok.AllArgsConstructor;
import lombok.Value;
import reactor.core.publisher.Mono;

@Value
@AllArgsConstructor
public class UserFacade {

    UserService userService;

    UserLookupRepo userLookupRepo;

    public Mono<UserDTO> getUser(String id) {
        UserId userId = UserId.of(id);

        return userService.get(userId)
                .map(UserTransformer::toApi);
    }

    public Mono<Void> create(String name, String password, Agent agent) {
        return userService.create(UserName.of(name), Password.of(password), agent).then();
    }

    public Mono<Void> rename(String id, long version, String name, Agent agent) {
        return userService.rename(UserId.of(id), Version.of(version), UserName.of(name), agent).then();
    }

    public Mono<Void> remove(String id, long version, Agent agent) {
        return userService.remove(UserId.of(id), Version.of(version), agent).then();
    }

    public Mono<AccessTokenDTO> login(String userName, CharSequence password) {
        return userLookupRepo.findUserIdByName(UserName.of(userName))
                .flatMap(userId -> userService.login(userId, Password.of(password)))
                .map(AccessTokenTransformer::toApi);
    }

}
