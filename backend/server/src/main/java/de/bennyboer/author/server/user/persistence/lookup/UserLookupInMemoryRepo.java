package de.bennyboer.author.server.user.persistence.lookup;

import de.bennyboer.author.user.User;
import de.bennyboer.author.user.UserName;
import de.bennyboer.common.UserId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserLookupInMemoryRepo implements UserLookupRepo {

    private final Map<UserName, UserId> userNameToUserId = new ConcurrentHashMap<>();

    @Override
    public Mono<UserId> findUserIdByName(UserName name) {
        return Mono.fromSupplier(() -> userNameToUserId.get(name));
    }

    @Override
    public Mono<Void> update(User user) {
        return Mono.fromRunnable(() -> userNameToUserId.put(user.getName(), user.getId()));
    }

    @Override
    public Mono<Void> remove(UserId userId) {
        return Flux.fromIterable(userNameToUserId.entrySet())
                .filter(entry -> entry.getValue().equals(userId))
                .next()
                .map(Map.Entry::getKey)
                .map(userNameToUserId::remove)
                .then();
    }

}
