package de.bennyboer.author.server.projects.persistence.lookup;

import de.bennyboer.author.project.ProjectId;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TestProjectLookupRepo extends InMemoryProjectLookupRepo {

    private final Map<String, CountDownLatch> awaitUpdateLatches = new ConcurrentHashMap<>();
    private final Map<String, CountDownLatch> awaitRemovalLatches = new ConcurrentHashMap<>();

    public void awaitUpdate(ProjectId projectId) {
        awaitUpdate(projectId, Duration.ofSeconds(5));
    }

    public void awaitUpdate(ProjectId projectId, Duration timeout) {
        var latch = new CountDownLatch(1);
        awaitUpdateLatches.put(projectId.getValue(), latch);

        if (get(projectId).block() != null) {
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

    public void awaitRemoval(ProjectId projectId) {
        awaitRemoval(projectId, Duration.ofSeconds(5));
    }

    public void awaitRemoval(ProjectId projectId, Duration timeout) {
        var latch = new CountDownLatch(1);
        awaitRemovalLatches.put(projectId.getValue(), latch);

        if (get(projectId).block() == null) {
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
    public Mono<Void> update(LookupProject readModel) {
        return super.update(readModel)
                .doOnSuccess((ignored) -> Optional.ofNullable(awaitUpdateLatches.get(readModel.getId().getValue()))
                        .ifPresent(CountDownLatch::countDown));
    }

    @Override
    public Mono<Void> remove(ProjectId projectId) {
        return super.remove(projectId)
                .doOnSuccess((ignored) -> Optional.ofNullable(awaitRemovalLatches.get(projectId.getValue()))
                        .ifPresent(CountDownLatch::countDown));
    }

}
