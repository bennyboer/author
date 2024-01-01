package de.bennyboer.author.server.users.persistence.lookup;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.eventsourcing.persistence.readmodel.EventSourcingReadModelRepo;
import de.bennyboer.author.user.UserName;
import reactor.core.publisher.Mono;

public interface UserLookupRepo extends EventSourcingReadModelRepo<UserId, LookupUser> {

    Mono<UserId> findUserIdByName(UserName name);

    Mono<Long> countUsers();

}
