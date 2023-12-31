package de.bennyboer.author.server.shared.persistence;

import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.eventsourcing.persistence.EventSourcingRepo;
import de.bennyboer.author.eventsourcing.persistence.InMemoryEventSourcingRepo;
import de.bennyboer.author.eventsourcing.persistence.SQLiteEventSourcingRepo;
import de.bennyboer.author.eventsourcing.serialization.EventSerializer;
import de.bennyboer.author.permissions.repo.InMemoryPermissionsRepo;
import de.bennyboer.author.permissions.repo.PermissionsRepo;
import de.bennyboer.author.permissions.repo.SQLitePermissionsRepo;

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

    public static PermissionsRepo createPermissionsRepo(String name) {
        if (isTestingProfile) {
            return new InMemoryPermissionsRepo();
        } else {
            return new SQLitePermissionsRepo(String.format("%s_permissions", name));
        }
    }

}
