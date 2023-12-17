package de.bennyboer.author.server.users;

import de.bennyboer.author.auth.token.TokenGenerator;
import de.bennyboer.author.server.shared.messaging.AggregateEventMessageListener;
import de.bennyboer.author.server.shared.messaging.AggregateEventPayloadTransformer;
import de.bennyboer.author.server.shared.modules.Module;
import de.bennyboer.author.server.shared.modules.ModuleConfig;
import de.bennyboer.author.server.users.facade.UsersFacade;
import de.bennyboer.author.server.users.messaging.UserCreatedUpdateLookupMsgListener;
import de.bennyboer.author.server.users.messaging.UserRemovedUpdateLookupMsgListener;
import de.bennyboer.author.server.users.persistence.lookup.UserLookupInMemoryRepo;
import de.bennyboer.author.server.users.rest.UsersRestHandler;
import de.bennyboer.author.server.users.rest.UsersRestRouting;
import de.bennyboer.author.server.users.transformer.UserEventTransformer;
import de.bennyboer.author.user.User;
import de.bennyboer.author.user.UserService;
import de.bennyboer.eventsourcing.aggregate.AggregateType;
import io.javalin.Javalin;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static io.javalin.apibuilder.ApiBuilder.path;

public class UsersModule extends Module {

    private final UsersFacade facade;

    public UsersModule(ModuleConfig config, TokenGenerator tokenGenerator) {
        super(config);

        var userService = new UserService(config.getEventSourcingRepo(), config.getEventPublisher(), tokenGenerator);
        var userLookupRepo = new UserLookupInMemoryRepo();
        facade = new UsersFacade(userService, userLookupRepo);
    }

    @Override
    public void apply(@NotNull Javalin javalin) {
        var restHandler = new UsersRestHandler(facade);
        var restRouting = new UsersRestRouting(restHandler);

        javalin.routes(() -> path("/api/users", restRouting));
    }

    @Override
    protected List<AggregateEventMessageListener> createMessageListeners() {
        return List.of(
                new UserCreatedUpdateLookupMsgListener(facade),
                new UserRemovedUpdateLookupMsgListener(facade)
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
        return facade.initDefaultUserIfNecessary();
    }

}
