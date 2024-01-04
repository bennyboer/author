package de.bennyboer.author.server;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import de.bennyboer.author.auth.keys.KeyPair;
import de.bennyboer.author.auth.keys.KeyPairs;
import de.bennyboer.author.auth.token.*;
import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.author.server.projects.ProjectsModule;
import de.bennyboer.author.server.shared.http.Auth;
import de.bennyboer.author.server.shared.http.security.Role;
import de.bennyboer.author.server.shared.messaging.Messaging;
import de.bennyboer.author.server.shared.modules.Module;
import de.bennyboer.author.server.shared.modules.ModuleConfig;
import de.bennyboer.author.server.shared.permissions.MissingPermissionException;
import de.bennyboer.author.server.shared.persistence.JsonMapperEventSerializer;
import de.bennyboer.author.server.shared.persistence.RepoFactory;
import de.bennyboer.author.server.shared.websocket.WebSocketService;
import de.bennyboer.author.server.structure.StructureModule;
import de.bennyboer.author.server.users.UsersConfig;
import de.bennyboer.author.server.users.UsersModule;
import de.bennyboer.author.server.users.persistence.lookup.SQLiteUserLookupRepo;
import de.bennyboer.author.server.users.transformer.UserEventTransformer;
import de.bennyboer.author.user.User;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.json.JavalinJackson;
import io.javalin.json.JsonMapper;
import io.javalin.plugin.bundled.CorsPluginConfig;
import io.javalin.security.RouteRole;
import lombok.Getter;

import java.util.List;
import java.util.Set;

import static de.bennyboer.author.server.shared.http.security.Role.UNAUTHORIZED;

public class App {

    private final AppConfig appConfig;

    @Getter
    private final JsonMapper jsonMapper;

    @Getter
    private final Messaging messaging;

    public App(AppConfig appConfig) {
        this.appConfig = appConfig;
        this.jsonMapper = createJsonMapper();
        this.messaging = setupMessaging();

        initAuth();

        if (appConfig.getProfile() == Profile.TESTING) {
            RepoFactory.setTestingProfile(true);
        }
    }

    private void initAuth() {
        TokenVerifier tokenVerifier = appConfig.getTokenVerifier();
        Token systemToken = appConfig.getTokenGenerator().generate(TokenContent.system()).block();
        Auth.init(tokenVerifier, systemToken);
    }

    private Messaging setupMessaging() {
        return new Messaging(jsonMapper);
    }

    private JsonMapper createJsonMapper() {
        return new JavalinJackson().updateMapper(mapper -> {
            mapper.registerModule(new Jdk8Module());
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        });
    }

    public Javalin createJavalin() {
        var webSocketService = new WebSocketService(messaging);

        return Javalin.create(config -> {
                    ModuleConfig moduleConfig = ModuleConfig.of(
                            messaging,
                            jsonMapper,
                            webSocketService,
                            appConfig
                    );

                    for (var moduleInstaller : appConfig.getModules()) {
                        Module module = moduleInstaller.install(moduleConfig);
                        config.plugins.register(module);
                    }

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
                .events(event -> {
                    event.serverStopping(messaging::stop);
                    event.serverStopped(RepoFactory::closeAll);
                })
                .exception(MissingPermissionException.class, (exception, ctx) -> ctx.status(403).result("Forbidden"));
    }

    public static void main(String[] args) {
        KeyPair keyPair = KeyPairs.read("/keys/key_pair.pem");
        TokenGenerator tokenGenerator = TokenGenerators.create(keyPair);
        TokenVerifier tokenVerifier = TokenVerifiers.create(keyPair);

        AppConfig config = AppConfig.builder()
                .tokenGenerator(tokenGenerator)
                .tokenVerifier(tokenVerifier)
                .modules(List.of(
                        (moduleConfig) -> {
                            var eventSerializer = new JsonMapperEventSerializer(
                                    moduleConfig.getJsonMapper(),
                                    UserEventTransformer::toSerialized,
                                    UserEventTransformer::fromSerialized
                            );
                            var eventSourcingRepo = RepoFactory.createEventSourcingRepo(User.TYPE, eventSerializer);

                            UsersConfig usersConfig = UsersConfig.builder()
                                    .tokenGenerator(tokenGenerator)
                                    .eventSourcingRepo(eventSourcingRepo)
                                    .permissionsRepo(RepoFactory.createPermissionsRepo("users"))
                                    .userLookupRepo(RepoFactory.createReadModelRepo(SQLiteUserLookupRepo::new))
                                    .build();

                            return new UsersModule(moduleConfig, usersConfig);
                        },
                        ProjectsModule::new,
                        StructureModule::new
                ))
                .build();

        App app = new App(config);

        Javalin javalin = app.createJavalin();
        javalin.start(config.getHost(), config.getPort());
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
