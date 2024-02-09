package de.bennyboer.author.server.assets;

import de.bennyboer.author.assets.Asset;
import de.bennyboer.author.assets.AssetId;
import de.bennyboer.author.assets.AssetsService;
import de.bennyboer.author.eventsourcing.aggregate.AggregateId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.author.server.assets.facade.AssetsCommandFacade;
import de.bennyboer.author.server.assets.facade.AssetsPermissionsFacade;
import de.bennyboer.author.server.assets.facade.AssetsQueryFacade;
import de.bennyboer.author.server.assets.facade.AssetsSyncFacade;
import de.bennyboer.author.server.assets.messaging.*;
import de.bennyboer.author.server.assets.permissions.AssetsPermissionsService;
import de.bennyboer.author.server.assets.rest.AssetsRestHandler;
import de.bennyboer.author.server.assets.rest.AssetsRestRouting;
import de.bennyboer.author.server.assets.transformer.AssetEventTransformer;
import de.bennyboer.author.server.shared.messaging.events.AggregateEventMessageListener;
import de.bennyboer.author.server.shared.messaging.events.AggregateEventPayloadTransformer;
import de.bennyboer.author.server.shared.messaging.permissions.MessagingAggregatePermissionsEventPublisher;
import de.bennyboer.author.server.shared.modules.AppPlugin;
import de.bennyboer.author.server.shared.modules.PluginConfig;
import de.bennyboer.author.server.shared.websocket.subscriptions.events.AggregateEventPermissionChecker;
import io.javalin.config.JavalinConfig;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static io.javalin.apibuilder.ApiBuilder.path;

public class AssetsPlugin extends AppPlugin {

    private final AssetsQueryFacade queryFacade;

    private final AssetsCommandFacade commandFacade;

    private final AssetsPermissionsFacade permissionsFacade;

    private final AssetsSyncFacade syncFacade;

    public AssetsPlugin(PluginConfig config, AssetsConfig assetsConfig) {
        super(config);

        var lookupRepo = assetsConfig.getAssetLookupRepo();

        var eventSourcingRepo = assetsConfig.getEventSourcingRepo();
        var assetsService = new AssetsService(eventSourcingRepo, getEventPublisher());

        var permissionsRepo = assetsConfig.getPermissionsRepo();
        var permissionsEventPublisher = new MessagingAggregatePermissionsEventPublisher(
                config.getMessaging(),
                config.getJsonMapper()
        );
        var assetsPermissionsService = new AssetsPermissionsService(permissionsRepo, permissionsEventPublisher);

        queryFacade = new AssetsQueryFacade(assetsService, assetsPermissionsService);
        commandFacade = new AssetsCommandFacade(assetsService, assetsPermissionsService, lookupRepo);
        permissionsFacade = new AssetsPermissionsFacade(assetsPermissionsService);
        syncFacade = new AssetsSyncFacade(assetsService, lookupRepo);
    }

    @Override
    public void onStart(@NotNull JavalinConfig config) {
        var restHandler = new AssetsRestHandler(queryFacade, commandFacade);
        var restRouting = new AssetsRestRouting(restHandler);

        config.router.apiBuilder(() -> path("/api/assets", restRouting));
    }

    @Override
    protected Map<AggregateType, AggregateEventPayloadTransformer> getAggregateEventPayloadTransformers() {
        return Map.of(Asset.TYPE, AssetEventTransformer::toApi);
    }

    @Override
    protected List<AggregateEventMessageListener> createMessageListeners() {
        return List.of(
                new UserCreatedAddPermissionToCreateAssetsMsgListener(permissionsFacade),
                new UserRemovedRemoveCreatePermissionMsgListener(permissionsFacade),
                new AssetCreatedAddPermissionForCreatorMsgListener(permissionsFacade),
                new AssetRemovedRemovePermissionsMsgListener(permissionsFacade),
                new UserRemovedRemoveOwnedAssetsMsgListener(commandFacade),
                new AssetUpdatedUpdateInLookupMsgListener(syncFacade),
                new UserImageUpdatedRemoveOldImageAssetMsgListener(commandFacade)
        );
    }

    @Override
    protected List<AggregateEventPermissionChecker> getEventPermissionCheckers() {
        return List.of(
                new AggregateEventPermissionChecker() {
                    @Override
                    public AggregateType getAggregateType() {
                        return Asset.TYPE;
                    }

                    @Override
                    public Mono<Boolean> hasPermissionToReceiveEvents(Agent agent, AggregateId aggregateId) {
                        return permissionsFacade.hasPermissionToReceiveEvents(
                                agent,
                                AssetId.of(aggregateId.getValue())
                        );
                    }
                }
        );
    }

}
