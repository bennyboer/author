package de.bennyboer.author.eventsourcing.sample.persistence;

import de.bennyboer.author.eventsourcing.persistence.readmodel.SQLiteEventSourcingReadModelRepo;
import reactor.core.publisher.Mono;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLiteEventSourcingReadModelTests extends EventSourcingReadModelTests {

    @Override
    protected SampleAggregateReadModelRepo createRepo() {
        return new SQLiteSampleAggregateReadModelRepo();
    }

    public static class SQLiteSampleAggregateReadModelRepo
            extends SQLiteEventSourcingReadModelRepo<String, SampleAggregateReadModel>
            implements SampleAggregateReadModelRepo {

        public SQLiteSampleAggregateReadModelRepo() {
            super("sample_aggregate_read_model", true);
        }

        @Override
        protected String getTableName() {
            return "sample_aggregate_read_model";
        }

        @Override
        protected String stringifyId(String id) {
            return id;
        }

        @Override
        protected void initialize(Connection connection) throws SQLException {
            String sql = """
                    CREATE TABLE %s (
                        id TEXT PRIMARY KEY,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL
                    )
                    """.formatted(getTableName());

            try (var statement = connection.createStatement()) {
                statement.execute(sql);
            }
        }

        @Override
        public Mono<Void> update(SampleAggregateReadModel readModel) {
            String sql = """
                    INSERT INTO %s (id, title, description) VALUES (?, ?, ?)
                    ON CONFLICT(id) DO UPDATE SET title = excluded.title, description = excluded.description
                    """.formatted(getTableName());

            return executeSqlUpdate(
                    sql,
                    statement -> {
                        statement.setString(1, readModel.getId());
                        statement.setString(2, readModel.getTitle());
                        statement.setString(3, readModel.getDescription());
                    }
            ).then();
        }

        @Override
        public Mono<SampleAggregateReadModel> get(String id) {
            String sql = """
                    SELECT * FROM %s
                    WHERE id = ?
                    """.formatted(getTableName());

            return executeSqlQueryWithOneResult(sql, statement -> {
                statement.setString(1, id);
            }, this::readSampleAggregate);
        }

        private SampleAggregateReadModel readSampleAggregate(ResultSet resultSet) throws SQLException {
            return SampleAggregateReadModel.of(
                    resultSet.getString("id"),
                    resultSet.getString("title"),
                    resultSet.getString("description")
            );
        }

    }

}
