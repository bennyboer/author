package de.bennyboer.author.server.shared.persistence;

import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.eventsourcing.persistence.EventSourcingRepo;
import de.bennyboer.author.eventsourcing.persistence.InMemoryEventSourcingRepo;
import de.bennyboer.author.eventsourcing.persistence.SQLiteEventSourcingRepo;
import de.bennyboer.author.eventsourcing.serialization.EventSerializer;
import de.bennyboer.author.permissions.repo.InMemoryPermissionsRepo;
import de.bennyboer.author.permissions.repo.PermissionsRepo;

public class RepoFactory {

    private static boolean isTestingProfile = false;

    public static void setTestingProfile(boolean isTestingProfile) {
        RepoFactory.isTestingProfile = isTestingProfile;
    }

    public static EventSourcingRepo createEventSourcingRepo(
            AggregateType aggregateType,
            EventSerializer eventSerializer
    ) {
        if (isTestingProfile) {
            return new InMemoryEventSourcingRepo();
        } else {
            return new SQLiteEventSourcingRepo(aggregateType, eventSerializer);
        }
    }

    public static PermissionsRepo createPermissionsRepo() {
        if (isTestingProfile) {
            return new InMemoryPermissionsRepo();
        } else {
            return new InMemoryPermissionsRepo(); // TODO To be replaced by an SQLite implementation
        }
    }

}
