package de.bennyboer.author.server.users.persistence.lookup;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.user.UserName;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TestUserLookupRepo extends InMemoryUserLookupRepo {

    private final Map<UserName, CountDownLatch> awaitUpdateLatches = new ConcurrentHashMap<>();
    private final Map<UserId, CountDownLatch> awaitRemovalLatches = new ConcurrentHashMap<>();

    public void awaitUpdate(UserName userName) {
        awaitUpdate(userName, Duration.ofSeconds(5));
    }

    public void awaitUpdate(UserName userName, Duration timeout) {
        var latch = new CountDownLatch(1);
        awaitUpdateLatches.put(userName, latch);

        if (findUserIdByName(userName).block() != null) {
            latch.countDown();
        }

        try {
            boolean reachedZero = latch.await(timeout.getSeconds(), TimeUnit.SECONDS);
            if (!reachedZero) {
                throw new RuntimeException("Timeout reached");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void awaitRemoval(UserId userId) {
        awaitRemoval(userId, Duration.ofSeconds(5));
    }

    public void awaitRemoval(UserId userId, Duration timeout) {
        var latch = new CountDownLatch(1);
        awaitRemovalLatches.put(userId, latch);

        if (lookup.get(userId) == null) {
            latch.countDown();
        }

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
                    var latch = awaitUpdateLatches.remove(readModel.getName());
                    if (latch != null) {
                        latch.countDown();
                    }
                });
    }

    @Override
    public Mono<Void> remove(UserId userId) {
        return super.remove(userId)
                .doOnSuccess(ignored -> {
                    var latch = awaitRemovalLatches.remove(userId);
                    if (latch != null) {
                        latch.countDown();
                    }
                });
    }

}
