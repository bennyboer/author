package de.bennyboer.author.server.structure;

import de.bennyboer.author.eventsourcing.aggregate.AggregateId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.author.server.shared.messaging.events.AggregateEventMessageListener;
import de.bennyboer.author.server.shared.messaging.events.AggregateEventPayloadTransformer;
import de.bennyboer.author.server.shared.messaging.permissions.MessagingAggregatePermissionsEventPublisher;
import de.bennyboer.author.server.shared.modules.Module;
import de.bennyboer.author.server.shared.modules.ModuleConfig;
import de.bennyboer.author.server.shared.websocket.subscriptions.events.AggregateEventPermissionChecker;
import de.bennyboer.author.server.structure.facade.StructureCommandFacade;
import de.bennyboer.author.server.structure.facade.StructurePermissionsFacade;
import de.bennyboer.author.server.structure.facade.StructureQueryFacade;
import de.bennyboer.author.server.structure.facade.StructureSyncFacade;
import de.bennyboer.author.server.structure.messaging.*;
import de.bennyboer.author.server.structure.permissions.StructurePermissionsService;
import de.bennyboer.author.server.structure.rest.StructureRestHandler;
import de.bennyboer.author.server.structure.rest.StructureRestRouting;
import de.bennyboer.author.server.structure.transformer.StructureEventTransformer;
import de.bennyboer.author.structure.Structure;
import de.bennyboer.author.structure.StructureId;
import de.bennyboer.author.structure.StructureService;
import io.javalin.Javalin;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static io.javalin.apibuilder.ApiBuilder.path;

public class StructureModule extends Module {

    private final StructureCommandFacade commandFacade;

    private final StructureQueryFacade queryFacade;

    private final StructureSyncFacade syncFacade;

    private final StructurePermissionsFacade permissionsFacade;

    public StructureModule(ModuleConfig config, StructureConfig structureConfig) {
        super(config);

        var eventSourcingRepo = structureConfig.getEventSourcingRepo();
        var structureService = new StructureService(eventSourcingRepo, getEventPublisher());

        var permissionsRepo = structureConfig.getPermissionsRepo();
        var permissionsEventPublisher = new MessagingAggregatePermissionsEventPublisher(
                config.getMessaging(),
                config.getJsonMapper()
        );
        var structurePermissionsService = new StructurePermissionsService(permissionsRepo, permissionsEventPublisher);

        var lookupRepo = structureConfig.getStructureLookupRepo();
        
        var projectDetailsService = structureConfig.getProjectDetailsService();

        commandFacade = new StructureCommandFacade(structureService, structurePermissionsService);
        queryFacade = new StructureQueryFacade(structureService, structurePermissionsService, lookupRepo);
        syncFacade = new StructureSyncFacade(structureService, lookupRepo, projectDetailsService);
        permissionsFacade = new StructurePermissionsFacade(structurePermissionsService);
    }

    @Override
    public void apply(Javalin javalin) {
        var restHandler = new StructureRestHandler(queryFacade, commandFacade);
        var restRouting = new StructureRestRouting(restHandler);

        javalin.routes(() -> path("/api/structures", restRouting));
    }

    @Override
    protected List<AggregateEventMessageListener> createMessageListeners() {
        return List.of(
                new ProjectCreatedCreateStructureMsgListener(syncFacade),
                new ProjectRemovedRemoveStructureMsgListener(syncFacade),
                new StructureCreatedAddPermissionsMsgListener(permissionsFacade),
                new StructureRemovedRemovePermissionsMsgListener(permissionsFacade),
                new UserRemovedRemovePermissionsMsgListener(permissionsFacade),
                new StructureCreatedAddToLookupMsgListener(syncFacade),
                new StructureRemovedRemoveFromLookupMsgListener(syncFacade)
        );
    }

    @Override
    protected List<AggregateEventPermissionChecker> getEventPermissionCheckers() {
        return List.of(
                new AggregateEventPermissionChecker() {
                    @Override
                    public AggregateType getAggregateType() {
                        return Structure.TYPE;
                    }

                    @Override
                    public Mono<Boolean> hasPermissionToReceiveEvents(Agent agent, AggregateId aggregateId) {
                        return permissionsFacade.hasPermissionToReceiveEvents(
                                agent,
                                StructureId.of(aggregateId.getValue())
                        );
                    }
                }
        );
    }

    @Override
    protected Map<AggregateType, AggregateEventPayloadTransformer> getAggregateEventPayloadTransformers() {
        return Map.of(Structure.TYPE, StructureEventTransformer::toApi);
    }

}
