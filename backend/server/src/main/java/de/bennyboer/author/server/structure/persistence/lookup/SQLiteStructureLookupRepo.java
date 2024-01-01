package de.bennyboer.author.server.structure.persistence.lookup;

import de.bennyboer.author.eventsourcing.persistence.readmodel.SQLiteEventSourcingReadModelRepo;
import de.bennyboer.author.structure.StructureId;
import reactor.core.publisher.Mono;

import java.sql.Connection;
import java.sql.SQLException;

public class SQLiteStructureLookupRepo extends SQLiteEventSourcingReadModelRepo<StructureId, LookupStructure>
        implements StructureLookupRepo {

    public SQLiteStructureLookupRepo(boolean isTemporary) {
        super("structure_lookup", isTemporary);
    }

    public SQLiteStructureLookupRepo() {
        this(false);
    }

    @Override
    protected String getTableName() {
        return "structure_lookup";
    }

    @Override
    protected String stringifyId(StructureId structureId) {
        return structureId.getValue();
    }

    @Override
    protected void initialize(Connection connection) throws SQLException {
        try (var statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE %s (
                        id TEXT PRIMARY KEY,
                        project_id TEXT NOT NULL
                    )
                    """.formatted(getTableName()));

            statement.execute("""
                    CREATE INDEX %s_project_id_idx ON %s (project_id)
                    """.formatted(getTableName(), getTableName()));
        }
    }

    @Override
    public Mono<Void> update(LookupStructure readModel) {
        String sql = """
                INSERT INTO %s (id, project_id)
                VALUES (?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    project_id = excluded.project_id
                """.formatted(getTableName());

        return update(sql, statement -> {
            statement.setString(1, readModel.getId().getValue());
            statement.setString(2, readModel.getProjectId());
        }).then();
    }

    @Override
    public Mono<StructureId> findStructureIdByProjectId(String projectId) {
        String sql = """
                SELECT id FROM %s WHERE project_id = ?
                """.formatted(getTableName());

        return queryOne(
                sql,
                statement -> statement.setString(1, projectId),
                resultSet -> StructureId.of(resultSet.getString("id"))
        );
    }

}
