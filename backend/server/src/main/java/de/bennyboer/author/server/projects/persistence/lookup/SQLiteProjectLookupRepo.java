package de.bennyboer.author.server.projects.persistence.lookup;

import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.persistence.readmodel.SQLiteEventSourcingReadModelRepo;
import de.bennyboer.author.project.ProjectId;
import de.bennyboer.author.project.ProjectName;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Collection;
import java.util.stream.Collectors;

public class SQLiteProjectLookupRepo extends SQLiteEventSourcingReadModelRepo<ProjectId, LookupProject>
        implements ProjectLookupRepo {

    public SQLiteProjectLookupRepo(boolean isTemporary) {
        super("project_lookup", isTemporary);
    }

    public SQLiteProjectLookupRepo() {
        this(false);
    }

    @Override
    protected String getTableName() {
        return "project_lookup";
    }

    @Override
    protected String stringifyId(ProjectId projectId) {
        return projectId.getValue();
    }

    @Override
    protected void initialize(Connection connection) throws SQLException {
        try (var statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE %s (
                        id TEXT PRIMARY KEY,
                        version INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        created_at TEXT NOT NULL
                    )
                    """.formatted(getTableName()));
        }
    }

    @Override
    public Mono<Void> update(LookupProject readModel) {
        String sql = """
                INSERT INTO %s (id, version, name, created_at)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    version = excluded.version,
                    name = excluded.name,
                    created_at = excluded.created_at
                """.formatted(getTableName());

        return update(sql, statement -> {
            statement.setString(1, readModel.getId().getValue());
            statement.setLong(2, readModel.getVersion().getValue());
            statement.setString(3, readModel.getName().getValue());
            statement.setString(4, readModel.getCreatedAt().toString());
        }).then();
    }

    @Override
    public Flux<LookupProject> getProjects(Collection<ProjectId> ids) {
        String joinedIds = ids.stream()
                .map(ProjectId::getValue)
                .map("'%s'"::formatted)
                .collect(Collectors.joining(", "));

        String sql = """
                SELECT id, version, name, created_at
                FROM %s
                WHERE id IN (%s)
                """.formatted(getTableName(), joinedIds);

        return query(
                sql,
                statement -> {
                },
                this::resultSetToLookupProject
        );
    }

    private LookupProject resultSetToLookupProject(ResultSet resultSet) throws SQLException {
        var id = ProjectId.of(resultSet.getString("id"));
        var version = Version.of(resultSet.getLong("version"));
        var name = ProjectName.of(resultSet.getString("name"));
        var createdAt = Instant.parse(resultSet.getString("created_at"));

        return LookupProject.of(id, version, name, createdAt);
    }

}
