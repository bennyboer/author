package de.bennyboer.author.server.assets.persistence.lookup;

import de.bennyboer.author.assets.AssetId;
import de.bennyboer.author.assets.Owner;
import de.bennyboer.author.common.UserId;
import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.persistence.readmodel.SQLiteEventSourcingReadModelRepo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLiteAssetLookupRepo extends SQLiteEventSourcingReadModelRepo<AssetId, LookupAsset>
        implements AssetLookupRepo {

    public SQLiteAssetLookupRepo(boolean isTemporary) {
        super("asset_lookup", isTemporary);
    }

    public SQLiteAssetLookupRepo() {
        this(false);
    }

    @Override
    protected String getTableName() {
        return "asset_lookup";
    }

    @Override
    protected String stringifyId(AssetId assetId) {
        return assetId.getValue();
    }

    @Override
    protected void initialize(Connection connection) throws SQLException {
        try (var statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE %s (
                        id TEXT PRIMARY KEY,
                        version INTEGER NOT NULL,
                        ownerId TEXT NOT NULL
                    )
                    """.formatted(getTableName()));

            statement.execute("""
                    CREATE INDEX %s_ownerId_idx ON %s (ownerId)
                    """.formatted(getTableName(), getTableName()));
        }
    }

    @Override
    public Mono<Void> update(LookupAsset readModel) {
        String sql = """
                INSERT INTO %s (id, version, ownerId)
                VALUES (?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    version = excluded.version,
                    ownerId = excluded.ownerId
                """.formatted(getTableName());

        return update(sql, statement -> {
            statement.setString(1, readModel.getId().getValue());
            statement.setLong(2, readModel.getVersion().getValue());
            statement.setString(3, readModel.getOwner().getUserId().getValue());
        }).then();
    }

    @Override
    public Flux<LookupAsset> findAssetsOwnedBy(Owner owner) {
        String sql = """
                SELECT id, version, ownerId
                FROM %s
                WHERE ownerId = ?
                """.formatted(getTableName());

        return query(
                sql,
                statement -> statement.setString(1, owner.getUserId().getValue()),
                this::resultSetToLookupAsset
        );
    }

    private LookupAsset resultSetToLookupAsset(ResultSet resultSet) throws SQLException {
        AssetId assetId = AssetId.of(resultSet.getString("id"));
        Version version = Version.of(resultSet.getInt("version"));
        Owner owner = Owner.of(UserId.of(resultSet.getString("ownerId")));

        return LookupAsset.of(assetId, version, owner);
    }

}
