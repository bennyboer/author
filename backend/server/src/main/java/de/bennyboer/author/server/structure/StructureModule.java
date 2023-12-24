package de.bennyboer.author.server.structure;

import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.eventsourcing.persistence.InMemoryEventSourcingRepo;
import de.bennyboer.author.permissions.repo.InMemoryPermissionsRepo;
import de.bennyboer.author.server.shared.messaging.AggregateEventMessageListener;
import de.bennyboer.author.server.shared.messaging.AggregateEventPayloadTransformer;
import de.bennyboer.author.server.shared.modules.Module;
import de.bennyboer.author.server.shared.modules.ModuleConfig;
import de.bennyboer.author.server.shared.permissions.MessagingAggregatePermissionsEventPublisher;
import de.bennyboer.author.server.structure.facade.TreeCommandFacade;
import de.bennyboer.author.server.structure.facade.TreePermissionsFacade;
import de.bennyboer.author.server.structure.facade.TreeQueryFacade;
import de.bennyboer.author.server.structure.facade.TreeSyncFacade;
import de.bennyboer.author.server.structure.messaging.ProjectCreatedCreateTreeMsgListener;
import de.bennyboer.author.server.structure.messaging.UserRemovedRemovePermissionsMsgListener;
import de.bennyboer.author.server.structure.permissions.TreePermissionsService;
import de.bennyboer.author.server.structure.rest.StructureRestRouting;
import de.bennyboer.author.server.structure.rest.TreeRestHandler;
import de.bennyboer.author.server.structure.transformer.TreeEventTransformer;
import de.bennyboer.author.structure.tree.Tree;
import de.bennyboer.author.structure.tree.TreeService;
import io.javalin.Javalin;
import org.jetbrains.annotations.NotNull;

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

        commandFacade = new TreeCommandFacade(treeService, treePermissionsService);
        queryFacade = new TreeQueryFacade(treeService, treePermissionsService);
        syncFacade = new TreeSyncFacade(treeService);
        permissionsFacade = new TreePermissionsFacade(treePermissionsService);
    }

    @Override
    public void apply(@NotNull Javalin javalin) {
        var restHandler = new TreeRestHandler(queryFacade, commandFacade);
        var restRouting = new StructureRestRouting(restHandler);

        javalin.routes(() -> path("/api/structure", restRouting));
    }

    @Override
    protected List<AggregateEventMessageListener> createMessageListeners() {
        return List.of(
                new ProjectCreatedCreateTreeMsgListener(syncFacade),
                new UserRemovedRemovePermissionsMsgListener(permissionsFacade)
        );
        // TODO Add listener to remove tree on project delete
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
