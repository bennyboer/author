package de.bennyboer.author.server.users;

import de.bennyboer.author.server.users.persistence.lookup.LookupUser;
import de.bennyboer.author.server.users.persistence.lookup.SQLiteUserLookupRepo;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class TestUserLookupRepo extends SQLiteUserLookupRepo {

    private final Map<Predicate<LookupUser>, CountDownLatch> awaitUpdateLatches = new HashMap<>();

    public TestUserLookupRepo() {
        super(true);
    }

    public void awaitUpdate(Predicate<LookupUser> userPredicate) {
        awaitUpdate(userPredicate, Duration.ofSeconds(5));
    }

    public void awaitUpdate(Predicate<LookupUser> userPredicate, Duration timeout) {
        var latch = new CountDownLatch(1);
        awaitUpdateLatches.put(userPredicate, latch);

        try {
            boolean reachedZero = latch.await(timeout.getSeconds(), TimeUnit.SECONDS);
            if (!reachedZero) {
                throw new RuntimeException("Timeout reached");
            }
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
