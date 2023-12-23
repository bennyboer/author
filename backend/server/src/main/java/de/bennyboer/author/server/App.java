package de.bennyboer.author.server;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import de.bennyboer.author.auth.keys.KeyPair;
import de.bennyboer.author.auth.keys.KeyPairs;
import de.bennyboer.author.auth.token.TokenGenerator;
import de.bennyboer.author.auth.token.TokenGenerators;
import de.bennyboer.author.auth.token.TokenVerifier;
import de.bennyboer.author.auth.token.TokenVerifiers;
import de.bennyboer.author.server.projects.ProjectsModule;
import de.bennyboer.author.server.shared.http.Auth;
import de.bennyboer.author.server.shared.http.security.Role;
import de.bennyboer.author.server.shared.messaging.Messaging;
import de.bennyboer.author.server.shared.messaging.MessagingEventPublisher;
import de.bennyboer.author.server.shared.modules.Module;
import de.bennyboer.author.server.shared.modules.ModuleConfig;
import de.bennyboer.author.server.shared.websocket.WebSocketService;
import de.bennyboer.author.server.structure.StructureModule;
import de.bennyboer.author.server.users.UsersModule;
import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.author.eventsourcing.persistence.InMemoryEventSourcingRepo;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.json.JavalinJackson;
import io.javalin.json.JsonMapper;
import io.javalin.plugin.bundled.CorsPluginConfig;
import io.javalin.security.RouteRole;

import java.util.Set;

import static de.bennyboer.author.server.shared.http.security.Role.UNAUTHORIZED;

public class App {

    public static void main(String[] args) {
        JsonMapper jsonMapper = new JavalinJackson().updateMapper(mapper -> {
            mapper.registerModule(new Jdk8Module());
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        });

        KeyPair keyPair = KeyPairs.read("/keys/key_pair.pem");
        TokenGenerator tokenGenerator = TokenGenerators.create(keyPair);
        TokenVerifier tokenVerifier = TokenVerifiers.create(keyPair);
        Auth.init(tokenVerifier);

        var messaging = new Messaging(jsonMapper);

        var eventSourcingRepo = new InMemoryEventSourcingRepo();
        var eventPublisher = new MessagingEventPublisher(messaging, jsonMapper);

        var webSocketService = new WebSocketService(messaging);

        Javalin.create(config -> {
                    ModuleConfig moduleConfig = ModuleConfig.of(eventSourcingRepo, eventPublisher, messaging);

                    registerModule(config, new UsersModule(moduleConfig, tokenGenerator));
                    registerModule(config, new ProjectsModule(moduleConfig));
                    registerModule(config, new StructureModule(moduleConfig));

                    config.plugins.enableCors(cors -> {
                        // TODO Restrict to frontend host and only allow for DEV build
                        cors.add(CorsPluginConfig::anyHost);
                    });

                    config.jsonMapper(jsonMapper);

                    config.accessManager(App::handleIfPermitted);
                })
                .get("/", ctx -> ctx.result("Hello World")) // TODO Maybe serve frontend here?
                .ws("/ws", ws -> {
                    ws.onConnect(webSocketService::onConnect);
                    ws.onClose(webSocketService::onClose);
                    ws.onError(webSocketService::onError);
                    ws.onMessage(webSocketService::onMessage);
                }, UNAUTHORIZED)
                .events(event -> event.serverStopping(messaging::stop))
                .start(7070);
    }

    private static void registerModule(JavalinConfig config, Module module) {
        config.plugins.register(module);
    }

    private static void handleIfPermitted(Handler handler, Context ctx, Set<? extends RouteRole> permittedRoles)
            throws Exception {
        Set<Role> defaultRoles = Set.of(Role.AUTHORIZED); // We require authorization by default
        Set<? extends RouteRole> roles = permittedRoles.isEmpty() ? defaultRoles : permittedRoles;

        /*
        If the route is permitted for unauthorized use or the agent is a system agent, we allow the request through.
         */
        Agent agent = Auth.toAgent(ctx).block();
        if (roles.contains(UNAUTHORIZED) || agent.isSystem()) {
            handler.handle(ctx);
            return;
        }

        /*
        Here we only allow users that are authorized and thus must have a user ID!
         */
        boolean isUserAgent = agent.getUserId().isPresent();
        if (!isUserAgent) {
            ctx.status(401).result("Unauthorized");
            return;
        }

        handler.handle(ctx);
    }

}
