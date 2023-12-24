package de.bennyboer.author.server.users;

import de.bennyboer.author.auth.token.TokenGenerator;
import de.bennyboer.author.common.UserId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.author.eventsourcing.persistence.InMemoryEventSourcingRepo;
import de.bennyboer.author.permissions.repo.InMemoryPermissionsRepo;
import de.bennyboer.author.server.shared.messaging.AggregateEventMessageListener;
import de.bennyboer.author.server.shared.messaging.AggregateEventPayloadTransformer;
import de.bennyboer.author.server.shared.modules.Module;
import de.bennyboer.author.server.shared.modules.ModuleConfig;
import de.bennyboer.author.server.shared.permissions.MessagingAggregatePermissionsEventPublisher;
import de.bennyboer.author.server.shared.websocket.subscriptions.EventPermissionChecker;
import de.bennyboer.author.server.users.facade.*;
import de.bennyboer.author.server.users.messaging.UserCreatedAddPermissionsMsgListener;
import de.bennyboer.author.server.users.messaging.UserCreatedUpdateLookupMsgListener;
import de.bennyboer.author.server.users.messaging.UserRemovedRemovePermissionsMsgListener;
import de.bennyboer.author.server.users.messaging.UserRemovedUpdateLookupMsgListener;
import de.bennyboer.author.server.users.permissions.UserPermissionsService;
import de.bennyboer.author.server.users.persistence.lookup.UserLookupInMemoryRepo;
import de.bennyboer.author.server.users.rest.UsersRestHandler;
import de.bennyboer.author.server.users.rest.UsersRestRouting;
import de.bennyboer.author.server.users.transformer.UserEventTransformer;
import de.bennyboer.author.user.User;
import de.bennyboer.author.user.UserService;
import io.javalin.Javalin;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static io.javalin.apibuilder.ApiBuilder.path;

public class UsersModule extends Module {

    private final UsersCommandFacade commandFacade;

    private final UsersQueryFacade queryFacade;

    private final UsersStartupFacade startupFacade;

    private final UsersSyncFacade syncFacade;

    private final UsersPermissionsFacade permissionsFacade;

    public UsersModule(ModuleConfig config, TokenGenerator tokenGenerator) {
        super(config);

        var eventSourcingRepo = new InMemoryEventSourcingRepo(); // TODO Use persistent repo
        var userService = new UserService(eventSourcingRepo, getEventPublisher(), tokenGenerator);

        var permissionsRepo = new InMemoryPermissionsRepo(); // TODO Use persistent repo
        var permissionsEventPublisher = new MessagingAggregatePermissionsEventPublisher(
                config.getMessaging(),
                config.getJsonMapper()
        );
        var userPermissionsService = new UserPermissionsService(permissionsRepo, permissionsEventPublisher);

        var userLookupRepo = new UserLookupInMemoryRepo(); // TODO Use persistent repo

        commandFacade = new UsersCommandFacade(userService, userPermissionsService, userLookupRepo);
        queryFacade = new UsersQueryFacade(userService, userPermissionsService);
        startupFacade = new UsersStartupFacade(userService, userLookupRepo);
        syncFacade = new UsersSyncFacade(userService, userLookupRepo);
        permissionsFacade = new UsersPermissionsFacade(userPermissionsService);
    }

    @Override
    public void apply(@NotNull Javalin javalin) {
        var restHandler = new UsersRestHandler(queryFacade, commandFacade);
        var restRouting = new UsersRestRouting(restHandler);

        javalin.routes(() -> path("/api/users", restRouting));
    }

    @Override
    protected List<AggregateEventMessageListener> createMessageListeners() {
        return List.of(
                new UserCreatedUpdateLookupMsgListener(syncFacade),
                new UserCreatedAddPermissionsMsgListener(permissionsFacade),
                new UserRemovedUpdateLookupMsgListener(syncFacade),
                new UserRemovedRemovePermissionsMsgListener(permissionsFacade)
        );
    }

    @Override
    protected List<EventPermissionChecker> getEventPermissionCheckers() {
        return List.of(
                new EventPermissionChecker() {
                    @Override
                    public AggregateType getAggregateType() {
                        return User.TYPE;
                    }

                    @Override
                    public Mono<Boolean> hasPermissionToReceiveEvents(Agent agent, AggregateId aggregateId) {
                        return permissionsFacade.hasPermissionToReceiveEvents(agent, UserId.of(aggregateId.getValue()));
                    }
                }
        );
    }

    @Override
    protected List<AggregateType> getAggregateTypes() {
        return List.of(User.TYPE);
    }

    @Override
    protected Map<AggregateType, AggregateEventPayloadTransformer> getAggregateEventPayloadTransformers() {
        return Map.of(User.TYPE, UserEventTransformer::toApi);
    }

    @Override
    protected Mono<Void> onServerStarted() {
        return startupFacade.initDefaultUserIfNecessary();
    }

}
