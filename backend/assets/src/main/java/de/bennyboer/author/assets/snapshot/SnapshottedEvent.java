package de.bennyboer.author.assets.snapshot;

import de.bennyboer.author.assets.AssetEvent;
import de.bennyboer.author.assets.Content;
import de.bennyboer.author.assets.Owner;
import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.Event;
import de.bennyboer.author.eventsourcing.event.EventName;
import de.bennyboer.author.eventsourcing.event.SnapshotEvent;
import jakarta.annotation.Nullable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;
import java.util.Optional;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SnapshottedEvent implements Event, SnapshotEvent {

    private static final Version VERSION = Version.zero();

    @Nullable
    Content content;

    Owner owner;

    Instant createdAt;

    @Nullable
    Instant removedAt;

    public static SnapshottedEvent of(
            @Nullable Content content,
            Owner owner,
            Instant createdAt,
            @Nullable Instant removedAt
    ) {
        checkNotNull(owner, "Owner must be given");
        checkNotNull(createdAt, "Created at must be given");

        return new SnapshottedEvent(content, owner, createdAt, removedAt);
    }

    public Optional<Content> getContent() {
        return Optional.ofNullable(content);
    }

    public Optional<Instant> getRemovedAt() {
        return Optional.ofNullable(removedAt);
    }

    @Override
    public EventName getEventName() {
        return AssetEvent.SNAPSHOTTED.getName();
    }

    @Override
    public Version getVersion() {
        return VERSION;
    }

}
