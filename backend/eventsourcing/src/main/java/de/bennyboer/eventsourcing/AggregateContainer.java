package de.bennyboer.eventsourcing;

import de.bennyboer.eventsourcing.api.Version;
import de.bennyboer.eventsourcing.api.aggregate.Aggregate;
import de.bennyboer.eventsourcing.api.aggregate.ApplyCommandResult;
import de.bennyboer.eventsourcing.api.command.Command;
import de.bennyboer.eventsourcing.api.event.Event;
import de.bennyboer.eventsourcing.api.event.metadata.EventMetadata;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;
import reactor.util.annotation.Nullable;

import java.util.Optional;

@Value
@With(AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AggregateContainer implements Aggregate {

    Aggregate aggregate;

    Version lastSnapshotVersion;

    @Nullable
    EventMetadata lastEventMetadata;

    public static AggregateContainer init(Aggregate aggregate) {
        return new AggregateContainer(aggregate, Version.zero(), null);
    }

    @Override
    public ApplyCommandResult apply(Command command) {
        return aggregate.apply(command);
    }

    @Override
    public AggregateContainer apply(Event event, EventMetadata metadata) {
        var updatedContainer = withAggregate(aggregate.apply(event, metadata))
                .withLastEventMetadata(metadata);

        if (metadata.isSnapshot()) {
            updatedContainer = updatedContainer.withLastSnapshotVersion(metadata.getAggregateVersion());
        }

        return updatedContainer;
    }

    @Override
    public int getCountOfEventsToSnapshotAfter() {
        return aggregate.getCountOfEventsToSnapshotAfter();
    }

    public Optional<EventMetadata> getLastEventMetadata() {
        return Optional.ofNullable(lastEventMetadata);
    }

    public boolean hasSeenEvents() {
        return getLastEventMetadata().isPresent();
    }

    public Version getVersion() {
        return getLastEventMetadata()
                .map(EventMetadata::getAggregateVersion)
                .orElse(Version.zero());
    }

    public long getVersionCountFromLastSnapshot() {
        return getVersion().getValue() - lastSnapshotVersion.getValue() + 1;
    }

}
