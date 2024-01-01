package de.bennyboer.author.server.users.persistence.lookup;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.eventsourcing.persistence.readmodel.InMemoryEventSourcingReadModelRepo;
import de.bennyboer.author.user.UserName;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

public class InMemoryUserLookupRepo extends InMemoryEventSourcingReadModelRepo<UserId, LookupUser>
        implements UserLookupRepo {

    @Override
    protected UserId getId(LookupUser readModel) {
        return readModel.getId();
    }

    @Override
    public Mono<UserId> findUserIdByName(UserName name) {
        return Flux.fromIterable(lookup.entrySet())
                .filter(entry -> entry.getValue().getName().equals(name))
                .map(Map.Entry::getKey)
                .next();
    }

    @Override
    public Mono<Long> countUsers() {
        return Mono.fromCallable(() -> (long) lookup.size());
    }

}
