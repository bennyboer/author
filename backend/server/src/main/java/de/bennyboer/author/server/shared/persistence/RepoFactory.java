package de.bennyboer.author.server.shared.persistence;

import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.eventsourcing.persistence.EventSourcingRepo;
import de.bennyboer.author.eventsourcing.persistence.SQLiteEventSourcingRepo;
import de.bennyboer.author.eventsourcing.persistence.readmodel.EventSourcingReadModelRepo;
import de.bennyboer.author.eventsourcing.serialization.EventSerializer;
import de.bennyboer.author.permissions.repo.PermissionsRepo;
import de.bennyboer.author.permissions.repo.SQLitePermissionsRepo;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

public class RepoFactory {

    private static boolean isTestingProfile = false;

    private static final List<AutoCloseable> closeables = new CopyOnWriteArrayList<>();

    public static void setTestingProfile(boolean isTestingProfile) {
        RepoFactory.isTestingProfile = isTestingProfile;
    }

    public static EventSourcingRepo createEventSourcingRepo(
            AggregateType aggregateType,
            EventSerializer eventSerializer
    ) {
        var repo = new SQLiteEventSourcingRepo(aggregateType, isTestingProfile, eventSerializer);
        addToCloseablesIfCloseable(repo);

        return repo;
    }

    public static PermissionsRepo createPermissionsRepo(String name) {
        var repo = new SQLitePermissionsRepo(String.format("%s_permissions", name), isTestingProfile);
        addToCloseablesIfCloseable(repo);

        return repo;
    }

    public static <ID, T, R extends EventSourcingReadModelRepo<ID, T>> R createReadModelRepo(
            Function<Boolean, R> repoGenerator
    ) {
        var repo = repoGenerator.apply(isTestingProfile);
        addToCloseablesIfCloseable(repo);

        return repo;
    }

    public static void closeAll() {
        closeables.forEach(closeable -> {
            try {
                closeable.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void addToCloseablesIfCloseable(Object potentialCloseable) {
        if (potentialCloseable instanceof AutoCloseable closeable) {
            closeables.add(closeable);
        }
    }

}
