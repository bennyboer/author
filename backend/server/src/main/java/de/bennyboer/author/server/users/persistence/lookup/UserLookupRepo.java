package de.bennyboer.author.server.users.persistence.lookup;

import de.bennyboer.author.user.User;
import de.bennyboer.author.user.UserName;
import de.bennyboer.common.UserId;
import reactor.core.publisher.Mono;

public interface UserLookupRepo {

    Mono<UserId> findUserIdByName(UserName name);

    Mono<Void> update(User user);

    Mono<Void> remove(UserId userId);

    Mono<Long> countUsers();

}
