package de.bennyboer.author.server.shared.persistence;

import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.eventsourcing.persistence.EventSourcingRepo;
import de.bennyboer.author.eventsourcing.persistence.InMemoryEventSourcingRepo;
import de.bennyboer.author.eventsourcing.persistence.SQLiteEventSourcingRepo;
import de.bennyboer.author.eventsourcing.persistence.readmodel.EventSourcingReadModelRepo;
import de.bennyboer.author.eventsourcing.serialization.EventSerializer;
import de.bennyboer.author.permissions.repo.InMemoryPermissionsRepo;
import de.bennyboer.author.permissions.repo.PermissionsRepo;
import de.bennyboer.author.permissions.repo.SQLitePermissionsRepo;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class RepoFactory {

    private static boolean isTestingProfile = false;

    public static void setTestingProfile(boolean isTestingProfile) {
        RepoFactory.isTestingProfile = isTestingProfile;
    }

    public static EventSourcingRepo createEventSourcingRepo(
            AggregateType aggregateType,
            EventSerializer eventSerializer,
            Consumer<AutoCloseable> closeableConsumer
    ) {
        if (isTestingProfile) {
            return new InMemoryEventSourcingRepo();
        }

        var repo = new SQLiteEventSourcingRepo(aggregateType, eventSerializer);
        closeableConsumer.accept(repo);
        return repo;
    }

    public static PermissionsRepo createPermissionsRepo(String name, Consumer<AutoCloseable> closeableConsumer) {
        if (isTestingProfile) {
            return new InMemoryPermissionsRepo();
        }

        var repo = new SQLitePermissionsRepo(String.format("%s_permissions", name));
        closeableConsumer.accept(repo);
        return repo;
    }

    public static <ID, T, R extends EventSourcingReadModelRepo<ID, T>> R createReadModelRepo(
            Supplier<R> testingRepoSupplier,
            Supplier<R> persistentRepoSupplier,
            Consumer<AutoCloseable> closeableConsumer
    ) {
        if (isTestingProfile) {
            return testingRepoSupplier.get();
        }

        var repo = persistentRepoSupplier.get();
        if (repo instanceof AutoCloseable) {
            closeableConsumer.accept((AutoCloseable) repo);
        }

        return repo;
    }

}
