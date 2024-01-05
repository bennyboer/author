package de.bennyboer.author.eventsourcing.persistence.readmodel;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public abstract class InMemoryEventSourcingReadModelRepo<ID, T> implements EventSourcingReadModelRepo<ID, T> {

    private final List<Awaiter<T>> updateAwaiters = new ArrayList<>();
    private final List<Awaiter<ID>> removalAwaiters = new ArrayList<>();

    protected final Map<ID, T> lookup = new HashMap<>();

    protected abstract ID getId(T readModel);

    public void awaitUpdate(ID id) {
        awaitUpdate(id, Duration.ofSeconds(5));
    }

    public void awaitUpdate(Predicate<T> predicate) {
        awaitUpdate(predicate, Duration.ofSeconds(5));
    }

    public void awaitUpdate(ID id, Duration timeout) {
        awaitUpdate(obj -> getId(obj).equals(id), timeout);
    }

    public void awaitUpdate(Predicate<T> predicate, Duration timeout) {
        Awaiter<T> awaiter = Awaiter.of(predicate);
        synchronized (updateAwaiters) {
            synchronized (lookup) {
                boolean isAlreadyFulfilled = lookup.values()
                        .stream()
                        .anyMatch(predicate);
                if (isAlreadyFulfilled) {
                    return;
                }

                updateAwaiters.add(awaiter);
            }
        }

        boolean reachedZero = awaiter.await(timeout);
        if (!reachedZero) {
            throw new RuntimeException("Timeout reached while awaiting update");
        }
    }

    public void awaitRemoval(ID id) {
        awaitRemoval(id, Duration.ofSeconds(5));
    }

    public void awaitRemoval(ID id, Duration timeout) {
        Awaiter<ID> awaiter = Awaiter.of(id::equals);
        synchronized (removalAwaiters) {
            synchronized (lookup) {
                boolean isAlreadyFulfilled = get(id).block() == null;
                if (isAlreadyFulfilled) {
                    return;
                }

                removalAwaiters.add(awaiter);
            }
        }

        boolean reachedZero = awaiter.await(timeout);
        if (!reachedZero) {
            throw new RuntimeException("Timeout reached while awaiting removal of ID '%s'".formatted(id.toString()));
        }
    }

    public Mono<T> get(ID id) {
        return Mono.fromCallable(() -> {
            synchronized (lookup) {
                return lookup.get(id);
            }
        });
    }

    @Override
    public Mono<Void> update(T readModel) {
        return Mono.fromRunnable(() -> {
            synchronized (lookup) {
                lookup.put(getId(readModel), readModel);

                synchronized (updateAwaiters) {
                    for (Awaiter<T> awaiter : updateAwaiters) {
                        awaiter.test(readModel);
                    }
                }
            }
        });
    }

    @Override
    public Mono<Void> remove(ID id) {
        return Mono.fromRunnable(() -> {
            synchronized (lookup) {
                lookup.remove(id);

                synchronized (removalAwaiters) {
                    for (Awaiter<ID> awaiter : removalAwaiters) {
                        awaiter.test(id);
                    }
                }
            }
        });
    }

    @Value
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static class Awaiter<T> {

        Predicate<T> predicate;

        CountDownLatch latch;

        public static <T> Awaiter<T> of(Predicate<T> predicate) {
            return new Awaiter<>(predicate, new CountDownLatch(1));
        }

        public void test(T value) {
            if (predicate.test(value)) {
                latch.countDown();
            }
        }

        public boolean await(Duration timeout) {
            try {
                return latch.await(timeout.getSeconds(), TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }

}
