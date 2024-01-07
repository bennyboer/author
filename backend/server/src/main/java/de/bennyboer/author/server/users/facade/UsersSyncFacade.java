package de.bennyboer.author.server.users.facade;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.server.users.persistence.lookup.LookupUser;
import de.bennyboer.author.server.users.persistence.lookup.UserLookupRepo;
import de.bennyboer.author.user.User;
import de.bennyboer.author.user.UserService;
import lombok.AllArgsConstructor;
import lombok.Value;
import reactor.core.publisher.Mono;

@Value
@AllArgsConstructor
public class UsersSyncFacade {

    UserService userService;

    UserLookupRepo userLookupRepo;

    public Mono<Void> updateUserLookupById(UserId userId) {
        return userService.get(userId)
                .map(this::toLookupUser)
                .flatMap(userLookupRepo::update)
                .then();
    }

    public Mono<Void> removeUserFromLookup(UserId userId) {
        return userLookupRepo.remove(userId);
    }

    private LookupUser toLookupUser(User user) {
        return LookupUser.of(user.getId(), user.getName(), user.getMail());
    }

}
