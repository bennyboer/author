package de.bennyboer.author.server.structure;

import de.bennyboer.author.eventsourcing.aggregate.AggregateId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.author.eventsourcing.persistence.InMemoryEventSourcingRepo;
import de.bennyboer.author.permissions.repo.InMemoryPermissionsRepo;
import de.bennyboer.author.server.shared.messaging.events.AggregateEventMessageListener;
import de.bennyboer.author.server.shared.messaging.events.AggregateEventPayloadTransformer;
import de.bennyboer.author.server.shared.messaging.permissions.MessagingAggregatePermissionsEventPublisher;
import de.bennyboer.author.server.shared.modules.Module;
import de.bennyboer.author.server.shared.modules.ModuleConfig;
import de.bennyboer.author.server.shared.websocket.subscriptions.events.AggregateEventPermissionChecker;
import de.bennyboer.author.server.structure.facade.TreeCommandFacade;
import de.bennyboer.author.server.structure.facade.TreePermissionsFacade;
import de.bennyboer.author.server.structure.facade.TreeQueryFacade;
import de.bennyboer.author.server.structure.facade.TreeSyncFacade;
import de.bennyboer.author.server.structure.messaging.*;
import de.bennyboer.author.server.structure.permissions.TreePermissionsService;
import de.bennyboer.author.server.structure.persistence.lookup.InMemoryTreeLookupRepo;
import de.bennyboer.author.server.structure.rest.StructureRestRouting;
import de.bennyboer.author.server.structure.rest.TreeRestHandler;
import de.bennyboer.author.server.structure.transformer.TreeEventTransformer;
import de.bennyboer.author.structure.tree.Tree;
import de.bennyboer.author.structure.tree.TreeId;
import de.bennyboer.author.structure.tree.TreeService;
import io.javalin.Javalin;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static io.javalin.apibuilder.ApiBuilder.path;

public class StructureModule extends Module {

    private final TreeCommandFacade commandFacade;

    private final TreeQueryFacade queryFacade;

    private final TreeSyncFacade syncFacade;

    private final TreePermissionsFacade permissionsFacade;

    public StructureModule(ModuleConfig config) {
        super(config);

        var eventSourcingRepo = new InMemoryEventSourcingRepo(); // TODO Use persistent repo
        var treeService = new TreeService(eventSourcingRepo, getEventPublisher());

        var permissionsRepo = new InMemoryPermissionsRepo(); // TODO Use persistent repo
        var permissionsEventPublisher = new MessagingAggregatePermissionsEventPublisher(
                config.getMessaging(),
                config.getJsonMapper()
        );
        var treePermissionsService = new TreePermissionsService(permissionsRepo, permissionsEventPublisher);

        var lookupRepo = new InMemoryTreeLookupRepo();

        commandFacade = new TreeCommandFacade(treeService, treePermissionsService);
        queryFacade = new TreeQueryFacade(treeService, treePermissionsService, lookupRepo);
        syncFacade = new TreeSyncFacade(treeService, lookupRepo);
        permissionsFacade = new TreePermissionsFacade(treePermissionsService);
    }

    @Override
    public void apply(Javalin javalin) {
        var restHandler = new TreeRestHandler(queryFacade, commandFacade);
        var restRouting = new StructureRestRouting(restHandler);

        javalin.routes(() -> path("/api/structure", restRouting));
    }

    @Override
    protected List<AggregateEventMessageListener> createMessageListeners() {
        return List.of(
                new ProjectCreatedCreateTreeMsgListener(syncFacade),
                new ProjectRemovedRemoveTreeMsgListener(syncFacade),
                new TreeCreatedAddPermissionsMsgListener(permissionsFacade),
                new TreeRemovedRemovePermissionsMsgListener(permissionsFacade),
                new UserRemovedRemovePermissionsMsgListener(permissionsFacade),
                new TreeCreatedAddToLookupMsgListener(syncFacade),
                new TreeRemovedRemoveFromLookupMsgListener(syncFacade)
        );
    }

    @Override
    protected List<AggregateEventPermissionChecker> getEventPermissionCheckers() {
        return List.of(
                new AggregateEventPermissionChecker() {
                    @Override
                    public AggregateType getAggregateType() {
                        return Tree.TYPE;
                    }

                    @Override
                    public Mono<Boolean> hasPermissionToReceiveEvents(Agent agent, AggregateId aggregateId) {
                        return permissionsFacade.hasPermissionToReceiveEvents(agent, TreeId.of(aggregateId.getValue()));
                    }
                }
        );
    }

    @Override
    protected List<AggregateType> getAggregateTypes() {
        return List.of(Tree.TYPE);
    }

    @Override
    protected Map<AggregateType, AggregateEventPayloadTransformer> getAggregateEventPayloadTransformers() {
        return Map.of(Tree.TYPE, TreeEventTransformer::toApi);
    }

}
