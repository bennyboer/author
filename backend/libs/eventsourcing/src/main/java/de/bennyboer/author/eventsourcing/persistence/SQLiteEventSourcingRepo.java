package de.bennyboer.author.eventsourcing.persistence;

import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.aggregate.AggregateId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.eventsourcing.event.Event;
import de.bennyboer.author.eventsourcing.event.EventName;
import de.bennyboer.author.eventsourcing.event.EventWithMetadata;
import de.bennyboer.author.eventsourcing.event.metadata.EventMetadata;
import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.author.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.author.eventsourcing.event.metadata.agent.AgentType;
import de.bennyboer.author.eventsourcing.serialization.EventSerializer;
import de.bennyboer.author.persistence.sqlite.SQLiteRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Locale;

public class SQLiteEventSourcingRepo extends SQLiteRepository implements EventSourcingRepo {

    private final EventSerializer eventSerializer;

    public SQLiteEventSourcingRepo(
            String name,
            boolean isTemporary,
            EventSerializer eventSerializer
    ) {
        super(name, isTemporary);

        this.eventSerializer = eventSerializer;
    }

    public SQLiteEventSourcingRepo(String name, EventSerializer eventSerializer) {
        this(name, false, eventSerializer);
    }

    public SQLiteEventSourcingRepo(
            AggregateType type,
            boolean isTemporary,
            EventSerializer eventSerializer
    ) {
        this(type.getValue().toLowerCase(Locale.ROOT), isTemporary, eventSerializer);
    }

    public SQLiteEventSourcingRepo(
            AggregateType type,
            EventSerializer eventSerializer
    ) {
        this(type, false, eventSerializer);
    }

    @Override
    public Mono<EventWithMetadata> insert(EventWithMetadata event) {
        EventMetadata metadata = event.getMetadata();
        Event ev = event.getEvent();

        String sql = """
                INSERT INTO events (
                    aggregate_id,
                    aggregate_type,
                    aggregate_version,
                    agent_type,
                    agent_id,
                    date,
                    is_snapshot,
                    event_name,
                    event_version,
                    event_payload
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        return executeSqlUpdate(sql, statement -> {
            String serializedEvent = eventSerializer.serialize(ev);

            statement.setString(1, metadata.getAggregateId().getValue());
            statement.setString(2, metadata.getAggregateType().getValue());
            statement.setLong(3, metadata.getAggregateVersion().getValue());
            statement.setString(4, metadata.getAgent().getType().name());
            statement.setString(5, metadata.getAgent().getId().getValue());
            statement.setString(6, metadata.getDate().toString());
            statement.setBoolean(7, metadata.isSnapshot());
            statement.setString(8, ev.getEventName().getValue());
            statement.setLong(9, ev.getVersion().getValue());
            statement.setString(10, serializedEvent);
        }).thenReturn(event);
    }

    @Override
    public Mono<EventWithMetadata> findNearestSnapshotEventByAggregateIdAndType(
            AggregateId aggregateId,
            AggregateType type,
            Version version
    ) {
        String sql = """
                SELECT * FROM events
                WHERE aggregate_id = ?
                AND aggregate_type = ?
                AND aggregate_version <= ?
                AND is_snapshot = 1
                ORDER BY aggregate_version DESC
                LIMIT 1
                """;

        return executeSqlQueryWithOneResult(sql, statement -> {
            statement.setString(1, aggregateId.getValue());
            statement.setString(2, type.getValue());
            statement.setLong(3, version.getValue());
        }, this::readEventWithMetadata);
    }

    @Override
    public Mono<EventWithMetadata> findLatestSnapshotEventByAggregateIdAndType(
            AggregateId aggregateId,
            AggregateType type
    ) {
        String sql = """
                SELECT * FROM events
                WHERE aggregate_id = ?
                AND aggregate_type = ?
                AND is_snapshot = 1
                ORDER BY aggregate_version DESC
                LIMIT 1
                """;

        return executeSqlQueryWithOneResult(sql, statement -> {
            statement.setString(1, aggregateId.getValue());
            statement.setString(2, type.getValue());
        }, this::readEventWithMetadata);
    }

    @Override
    public Flux<EventWithMetadata> findEventsByAggregateIdAndType(
            AggregateId aggregateId,
            AggregateType type,
            Version fromVersion
    ) {
        String sql = """
                SELECT * FROM events
                WHERE aggregate_id = ?
                AND aggregate_type = ?
                AND aggregate_version >= ?
                ORDER BY aggregate_version ASC
                """;

        return executeSqlQuery(sql, statement -> {
            statement.setString(1, aggregateId.getValue());
            statement.setString(2, type.getValue());
            statement.setLong(3, fromVersion.getValue());
        }, this::readEventWithMetadata);
    }

    @Override
    public Flux<EventWithMetadata> findEventsByAggregateIdAndTypeUntilVersion(
            AggregateId aggregateId,
            AggregateType type,
            Version fromVersion,
            Version untilVersion
    ) {
        String sql = """
                SELECT * FROM events
                WHERE aggregate_id = ?
                AND aggregate_type = ?
                AND aggregate_version >= ?
                AND aggregate_version <= ?
                ORDER BY aggregate_version ASC
                """;

        return executeSqlQuery(sql, statement -> {
            statement.setString(1, aggregateId.getValue());
            statement.setString(2, type.getValue());
            statement.setLong(3, fromVersion.getValue());
            statement.setLong(4, untilVersion.getValue());
        }, this::readEventWithMetadata);
    }

    @Override
    protected void initialize(Connection connection) throws SQLException {
        createEventsTableAndIndices(connection);
    }

    private void createEventsTableAndIndices(Connection connection) throws SQLException {
        try (var statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE events (
                        aggregate_id TEXT NOT NULL,
                        aggregate_type TEXT NOT NULL,
                        aggregate_version INTEGER NOT NULL,
                        agent_type TEXT NOT NULL,
                        agent_id TEXT,
                        date TEXT NOT NULL,
                        is_snapshot INTEGER NOT NULL DEFAULT 0,
                        event_name TEXT NOT NULL,
                        event_version INTEGER NOT NULL,
                        event_payload TEXT NOT NULL,
                        PRIMARY KEY (aggregate_type, aggregate_id, aggregate_version)
                    )
                    """);

            statement.execute("""
                    CREATE UNIQUE INDEX idx_events_snapshots 
                    ON events (
                        aggregate_id, 
                        aggregate_type, 
                        aggregate_version, 
                        is_snapshot
                    )
                    """);
        }
    }

    private EventWithMetadata readEventWithMetadata(ResultSet resultSet)
            throws SQLException {
        AggregateId aggregateId = AggregateId.of(resultSet.getString("aggregate_id"));
        AggregateType type = AggregateType.of(resultSet.getString("aggregate_type"));
        Version version = Version.of(resultSet.getLong("aggregate_version"));

        AgentType agentType = AgentType.valueOf(resultSet.getString("agent_type"));
        AgentId agentId = AgentId.of(resultSet.getString("agent_id"));
        Agent agent = Agent.of(agentType, agentId);

        Instant date = Instant.parse(resultSet.getString("date"));
        boolean isSnapshot = resultSet.getBoolean("is_snapshot");

        EventMetadata metadata = EventMetadata.of(aggregateId, type, version, agent, date, isSnapshot);

        EventName eventName = EventName.of(resultSet.getString("event_name"));
        Version eventVersion = Version.of(resultSet.getLong("event_version"));
        Event event = eventSerializer.deserialize(resultSet.getString("event_payload"), eventName, eventVersion);

        return EventWithMetadata.of(event, metadata);
    }

}
