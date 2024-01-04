package de.bennyboer.author.server.users;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.author.server.shared.messaging.events.AggregateEventMessageListener;
import de.bennyboer.author.server.shared.messaging.events.AggregateEventPayloadTransformer;
import de.bennyboer.author.server.shared.messaging.permissions.MessagingAggregatePermissionsEventPublisher;
import de.bennyboer.author.server.shared.modules.Module;
import de.bennyboer.author.server.shared.modules.ModuleConfig;
import de.bennyboer.author.server.shared.websocket.subscriptions.events.AggregateEventPermissionChecker;
import de.bennyboer.author.server.users.facade.*;
import de.bennyboer.author.server.users.messaging.UserCreatedAddPermissionsMsgListener;
import de.bennyboer.author.server.users.messaging.UserCreatedUpdateLookupMsgListener;
import de.bennyboer.author.server.users.messaging.UserRemovedRemovePermissionsMsgListener;
import de.bennyboer.author.server.users.messaging.UserRemovedUpdateLookupMsgListener;
import de.bennyboer.author.server.users.permissions.UserPermissionsService;
import de.bennyboer.author.server.users.rest.UsersRestHandler;
import de.bennyboer.author.server.users.rest.UsersRestRouting;
import de.bennyboer.author.server.users.transformer.UserEventTransformer;
import de.bennyboer.author.user.User;
import de.bennyboer.author.user.UserService;
import io.javalin.Javalin;
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

    public UsersModule(ModuleConfig config, UsersConfig usersConfig) {
        super(config);

        var eventSourcingRepo = usersConfig.getEventSourcingRepo();
        var userService = new UserService(
                eventSourcingRepo,
                getEventPublisher(),
                usersConfig.getTokenGenerator(),
                config.getAppConfig().getClock()
        );

        var permissionsRepo = usersConfig.getPermissionsRepo();
        var permissionsEventPublisher = new MessagingAggregatePermissionsEventPublisher(
                config.getMessaging(),
                config.getJsonMapper()
        );
        var userPermissionsService = new UserPermissionsService(permissionsRepo, permissionsEventPublisher);

        var userLookupRepo = usersConfig.getUserLookupRepo();

        commandFacade = new UsersCommandFacade(userService, userPermissionsService, userLookupRepo);
        queryFacade = new UsersQueryFacade(userService, userPermissionsService);
        startupFacade = new UsersStartupFacade(userService, userLookupRepo);
        syncFacade = new UsersSyncFacade(userService, userLookupRepo);
        permissionsFacade = new UsersPermissionsFacade(userPermissionsService);
    }

    @Override
    public void apply(Javalin javalin) {
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
    protected List<AggregateEventPermissionChecker> getEventPermissionCheckers() {
        return List.of(
                new AggregateEventPermissionChecker() {
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
