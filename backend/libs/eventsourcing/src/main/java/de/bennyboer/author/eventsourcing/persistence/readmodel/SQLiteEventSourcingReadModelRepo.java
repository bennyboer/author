package de.bennyboer.author.eventsourcing.persistence.readmodel;

import de.bennyboer.author.persistence.sqlite.SQLiteRepository;
import reactor.core.publisher.Mono;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class SQLiteEventSourcingReadModelRepo<ID, T> extends SQLiteRepository
        implements EventSourcingReadModelRepo<ID, T> {

    public SQLiteEventSourcingReadModelRepo(String name, boolean isTemporary) {
        super(name, isTemporary);
    }

    public SQLiteEventSourcingReadModelRepo(String name) {
        super(name);
    }

    protected abstract String getTableName();

    protected abstract String stringifyId(ID id);

    @Override
    protected abstract void initialize(Connection connection) throws SQLException;

    @Override
    public abstract Mono<Void> update(T readModel);

    @Override
    public Mono<Void> remove(ID id) {
        String sql = """
                DELETE FROM %s
                WHERE id = ?
                """.formatted(getTableName());

        return executeSqlUpdate(sql, statement -> statement.setString(1, stringifyId(id))).then();
    }

}
