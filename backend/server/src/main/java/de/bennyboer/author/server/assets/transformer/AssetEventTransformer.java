package de.bennyboer.author.server.assets.transformer;

import de.bennyboer.author.assets.AssetEvent;
import de.bennyboer.author.assets.Content;
import de.bennyboer.author.assets.ContentType;
import de.bennyboer.author.assets.Owner;
import de.bennyboer.author.assets.create.CreatedEvent;
import de.bennyboer.author.assets.remove.RemovedEvent;
import de.bennyboer.author.assets.snapshot.SnapshottedEvent;
import de.bennyboer.author.common.UserId;
import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.Event;
import de.bennyboer.author.eventsourcing.event.EventName;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AssetEventTransformer {

    public static Map<String, Object> toApi(Event event) {
        return Map.of();
    }

    public static Map<String, Object> toSerialized(Event event) {
        return switch (event) {
            case CreatedEvent createdEvent -> Map.of(
                    "content", createdEvent.getContent().getData(),
                    "contentType", createdEvent.getContent().getType().asMimeType()
            );
            case RemovedEvent ignoredEvent -> Map.of();
            case SnapshottedEvent snapshottedEvent -> {
                var result = new HashMap<String, Object>(Map.of(
                        "ownerId", snapshottedEvent.getOwner().getUserId().getValue(),
                        "createdAt", snapshottedEvent.getCreatedAt().toString()
                ));

                snapshottedEvent.getContent().ifPresent(content -> {
                    result.put("content", content.getData());
                    result.put("contentType", content.getType().asMimeType());
                });
                snapshottedEvent.getRemovedAt().ifPresent(removedAt -> result.put("removedAt", removedAt.toString()));

                yield result;
            }
            default -> throw new IllegalArgumentException("Unknown event type: " + event.getClass().getName());
        };
    }

    public static Event fromSerialized(Map<String, Object> serialized, EventName eventName, Version ignoredVersion) {
        AssetEvent event = AssetEvent.fromName(eventName);

        return switch (event) {
            case CREATED -> CreatedEvent.of(Content.of(
                    serialized.get("content").toString(),
                    ContentType.fromMimeType(serialized.get("contentType").toString())
            ));
            case REMOVED -> RemovedEvent.of();
            case SNAPSHOTTED -> {
                Content content = Optional.ofNullable(serialized.get("content"))
                        .map(contentData -> Content.of(
                                contentData.toString(),
                                ContentType.fromMimeType(serialized.get("contentType").toString())
                        ))
                        .orElse(null);
                Owner owner = Owner.of(UserId.of(serialized.get("ownerId").toString()));
                Instant createdAt = Instant.parse(serialized.get("createdAt").toString());
                Instant removedAt = Optional.ofNullable(serialized.get("removedAt"))
                        .map(Object::toString)
                        .map(Instant::parse)
                        .orElse(null);

                yield SnapshottedEvent.of(content, owner, createdAt, removedAt);
            }
        };
    }

}
