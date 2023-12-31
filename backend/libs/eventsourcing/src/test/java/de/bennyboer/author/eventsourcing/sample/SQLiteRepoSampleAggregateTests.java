package de.bennyboer.author.eventsourcing.sample;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.Event;
import de.bennyboer.author.eventsourcing.event.EventName;
import de.bennyboer.author.eventsourcing.persistence.EventSourcingRepo;
import de.bennyboer.author.eventsourcing.persistence.SQLiteEventSourcingRepo;
import de.bennyboer.author.eventsourcing.sample.events.*;
import de.bennyboer.author.eventsourcing.serialization.EventSerializer;

public class SQLiteRepoSampleAggregateTests extends SampleAggregateTests {

    @Override
    protected EventSourcingRepo createRepo() {
        ObjectMapper objectMapper = new ObjectMapper();

        return new SQLiteEventSourcingRepo(
                "test",
                true,
                new EventSerializer() {
                    @Override
                    public String serialize(Event event) {
                        try {
                            return objectMapper.writeValueAsString(event);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public Event deserialize(String event, EventName eventName, Version eventVersion) {
                        var clazz = switch (eventName.getValue()) {
                            case "CREATED" -> {
                                if (eventVersion.equals(CreatedEvent.VERSION)) {
                                    yield CreatedEvent.class;
                                } else if (eventVersion.equals(CreatedEvent2.VERSION)) {
                                    yield CreatedEvent2.class;
                                } else {
                                    throw new RuntimeException("Unknown event version: " + eventVersion);
                                }
                            }
                            case "DELETED" -> DeletedEvent.class;
                            case "SNAPSHOTTED" -> SnapshottedEvent.class;
                            case "DESCRIPTION_UPDATED" -> DescriptionUpdatedEvent.class;
                            case "TITLE_UPDATED" -> TitleUpdatedEvent.class;
                            default -> throw new RuntimeException("Unknown event name: " + eventName);
                        };

                        try {
                            return objectMapper.readValue(event, clazz);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
        );
    }

}
