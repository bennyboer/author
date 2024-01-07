package de.bennyboer.author.server.users.persistence.lookup;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.eventsourcing.persistence.readmodel.SQLiteEventSourcingReadModelRepo;
import de.bennyboer.author.user.Mail;
import de.bennyboer.author.user.UserName;
import reactor.core.publisher.Mono;

import java.sql.Connection;
import java.sql.SQLException;

public class SQLiteUserLookupRepo extends SQLiteEventSourcingReadModelRepo<UserId, LookupUser>
        implements UserLookupRepo {

    public SQLiteUserLookupRepo(boolean isTemporary) {
        super("user_lookup", isTemporary);
    }

    public SQLiteUserLookupRepo() {
        this(false);
    }

    @Override
    protected String getTableName() {
        return "user_lookup";
    }

    @Override
    protected String stringifyId(UserId userId) {
        return userId.getValue();
    }

    @Override
    protected void initialize(Connection connection) throws SQLException {
        try (var statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE %s (
                        id TEXT PRIMARY KEY,
                        name TEXT NOT NULL,
                        mail TEXT NOT NULL
                    )
                    """.formatted(getTableName()));

            statement.execute("""
                    CREATE UNIQUE INDEX %s_name_idx ON %s (name)
                    """.formatted(getTableName(), getTableName()));

            statement.execute("""
                    CREATE UNIQUE INDEX %s_mail_idx ON %s (mail)
                    """.formatted(getTableName(), getTableName()));
        }
    }

    @Override
    public Mono<Void> update(LookupUser readModel) {
        String sql = """
                INSERT INTO %s (id, name, mail)
                VALUES (?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                  name = excluded.name,
                  mail = excluded.mail
                """.formatted(getTableName());

        return update(sql, statement -> {
            statement.setString(1, stringifyId(readModel.getId()));
            statement.setString(2, readModel.getName().getValue());
            statement.setString(3, readModel.getMail().getValue());
        }).then();
    }

    @Override
    public Mono<UserId> findUserIdByName(UserName name) {
        String sql = """
                SELECT id
                FROM %s
                WHERE name = ?
                """.formatted(getTableName());

        return queryOne(
                sql,
                statement -> statement.setString(1, name.getValue()),
                resultSet -> UserId.of(resultSet.getString("id"))
        );
    }

    @Override
    public Mono<UserId> findUserIdByMail(Mail mail) {
        String sql = """
                SELECT id
                FROM %s
                WHERE mail = ?
                """.formatted(getTableName());

        return queryOne(
                sql,
                statement -> statement.setString(1, mail.getValue()),
                resultSet -> UserId.of(resultSet.getString("id"))
        );
    }

    @Override
    public Mono<Long> countUsers() {
        String sql = """
                SELECT COUNT(*)
                FROM %s
                """.formatted(getTableName());

        return queryOne(
                sql,
                statement -> {
                },
                resultSet -> resultSet.getLong(1)
        );
    }

}
