package de.bennyboer.author.server.users;

import de.bennyboer.author.server.users.persistence.lookup.LookupUser;
import de.bennyboer.author.server.users.persistence.lookup.SQLiteUserLookupRepo;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.function.Predicate;

public class TestUserLookupRepo extends SQLiteUserLookupRepo {

    private final Map<Predicate<LookupUser>, CountDownLatch> awaitUpdateLatches = new HashMap<>();

    public TestUserLookupRepo() {
        super(true);
    }

    public void awaitUpdate(Predicate<LookupUser> userPredicate) {
        var latch = new CountDownLatch(1);
        awaitUpdateLatches.put(userPredicate, latch);

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Mono<Void> update(LookupUser readModel) {
        return super.update(readModel)
                .doOnSuccess(ignored -> {
                    awaitUpdateLatches.forEach((predicate, latch) -> {
                        if (predicate.test(readModel)) {
                            latch.countDown();
                        }
                    });
                });
    }

}
