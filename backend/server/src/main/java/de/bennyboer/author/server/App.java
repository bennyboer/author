package de.bennyboer.author.server;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import de.bennyboer.author.assets.Asset;
import de.bennyboer.author.auth.keys.KeyPair;
import de.bennyboer.author.auth.keys.KeyPairs;
import de.bennyboer.author.auth.token.*;
import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.author.project.Project;
import de.bennyboer.author.server.assets.AssetsConfig;
import de.bennyboer.author.server.assets.AssetsPlugin;
import de.bennyboer.author.server.assets.persistence.lookup.SQLiteAssetLookupRepo;
import de.bennyboer.author.server.assets.transformer.AssetEventTransformer;
import de.bennyboer.author.server.projects.ProjectsConfig;
import de.bennyboer.author.server.projects.ProjectsPlugin;
import de.bennyboer.author.server.projects.persistence.lookup.SQLiteProjectLookupRepo;
import de.bennyboer.author.server.projects.transformer.ProjectEventTransformer;
import de.bennyboer.author.server.shared.http.Auth;
import de.bennyboer.author.server.shared.http.security.Role;
import de.bennyboer.author.server.shared.messaging.Messaging;
import de.bennyboer.author.server.shared.modules.AppPlugin;
import de.bennyboer.author.server.shared.modules.PluginConfig;
import de.bennyboer.author.server.shared.permissions.MissingPermissionException;
import de.bennyboer.author.server.shared.persistence.JsonMapperEventSerializer;
import de.bennyboer.author.server.shared.persistence.RepoFactory;
import de.bennyboer.author.server.shared.websocket.WebSocketService;
import de.bennyboer.author.server.structure.StructureConfig;
import de.bennyboer.author.server.structure.StructurePlugin;
import de.bennyboer.author.server.structure.external.project.ProjectDetailsHttpService;
import de.bennyboer.author.server.structure.persistence.lookup.SQLiteStructureLookupRepo;
import de.bennyboer.author.server.structure.transformer.StructureEventTransformer;
import de.bennyboer.author.server.users.UsersConfig;
import de.bennyboer.author.server.users.UsersPlugin;
import de.bennyboer.author.server.users.persistence.lookup.SQLiteUserLookupRepo;
import de.bennyboer.author.server.users.transformer.UserEventTransformer;
import de.bennyboer.author.structure.Structure;
import de.bennyboer.author.user.User;
import io.javalin.Javalin;
import io.javalin.community.ssl.SslPlugin;
import io.javalin.community.ssl.TlsConfig;
import io.javalin.http.Context;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.json.JavalinJackson;
import io.javalin.json.JsonMapper;
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
                    config.http.maxRequestSize = 16 * 1024 * 1024;
                    config.useVirtualThreads = true;
                    config.jsonMapper(jsonMapper);

                    config.bundledPlugins.enableCors(cors -> {
                        // TODO Restrict to frontend host and only allow for DEV build
                        cors.addRule(corsConfig -> {
                            corsConfig.anyHost();
                            corsConfig.exposeHeader("Location");
                        });
                    });

                    config.registerPlugin(new SslPlugin(conf -> {
                        conf.insecure = false;
                        conf.secure = true;
                        conf.http2 = true;
                        conf.redirect = true;
                        conf.tlsConfig = TlsConfig.MODERN;
                        conf.pemFromClasspath(
                                "/keys/cert.pem",
                                "/keys/key.pem",
                                "password"
                        ); // TODO Generate cert and private key file using openssh and provide passwort - via config!
                    }));

                    PluginConfig pluginConfig = PluginConfig.of(
                            messaging,
                            jsonMapper,
                            webSocketService,
                            appConfig
                    );

                    for (var moduleInstaller : appConfig.getPlugins()) {
                        AppPlugin plugin = moduleInstaller.install(pluginConfig);
                        config.registerPlugin(plugin);
                    }
                })
                .beforeMatched(App::checkIfAuthorized)
                .ws("/ws", ws -> { // TODO Replace by SSE in every module - this can be removed then
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
                .plugins(List.of(
                        (moduleConfig) -> configureUsersModule(moduleConfig, tokenGenerator),
                        App::configureProjectsModule,
                        App::configureStructureModule,
                        App::configureAssetsModule
                ))
                .build();

        App app = new App(config);

        Javalin javalin = app.createJavalin();
        javalin.start(config.getHost(), config.getPort());
    }

    private static AssetsPlugin configureAssetsModule(PluginConfig pluginConfig) {
        var eventSerializer = new JsonMapperEventSerializer(
                pluginConfig.getJsonMapper(),
                AssetEventTransformer::toSerialized,
                AssetEventTransformer::fromSerialized
        );
        var eventSourcingRepo = RepoFactory.createEventSourcingRepo(Asset.TYPE, eventSerializer);

        AssetsConfig assetsConfig = AssetsConfig.builder()
                .eventSourcingRepo(eventSourcingRepo)
                .permissionsRepo(RepoFactory.createPermissionsRepo("assets"))
                .assetLookupRepo(RepoFactory.createReadModelRepo(SQLiteAssetLookupRepo::new))
                .build();

        return new AssetsPlugin(pluginConfig, assetsConfig);
    }

    private static StructurePlugin configureStructureModule(PluginConfig pluginConfig) {
        var eventSerializer = new JsonMapperEventSerializer(
                pluginConfig.getJsonMapper(),
                StructureEventTransformer::toSerialized,
                StructureEventTransformer::fromSerialized
        );
        var eventSourcingRepo = RepoFactory.createEventSourcingRepo(Structure.TYPE, eventSerializer);

        var projectDetailsService = new ProjectDetailsHttpService(
                pluginConfig.getAppConfig().getHostUrl(),
                pluginConfig.getAppConfig().getHttpApi(),
                pluginConfig.getJsonMapper()
        );

        StructureConfig structureConfig = StructureConfig.builder()
                .eventSourcingRepo(eventSourcingRepo)
                .permissionsRepo(RepoFactory.createPermissionsRepo("structure"))
                .structureLookupRepo(RepoFactory.createReadModelRepo(SQLiteStructureLookupRepo::new))
                .projectDetailsService(projectDetailsService)
                .build();

        return new StructurePlugin(pluginConfig, structureConfig);
    }

    private static ProjectsPlugin configureProjectsModule(PluginConfig pluginConfig) {
        var eventSerializer = new JsonMapperEventSerializer(
                pluginConfig.getJsonMapper(),
                ProjectEventTransformer::toSerialized,
                ProjectEventTransformer::fromSerialized
        );
        var eventSourcingRepo = RepoFactory.createEventSourcingRepo(Project.TYPE, eventSerializer);

        ProjectsConfig projectsConfig = ProjectsConfig.builder()
                .eventSourcingRepo(eventSourcingRepo)
                .permissionsRepo(RepoFactory.createPermissionsRepo("projects"))
                .projectLookupRepo(RepoFactory.createReadModelRepo(SQLiteProjectLookupRepo::new))
                .build();

        return new ProjectsPlugin(pluginConfig, projectsConfig);
    }

    private static UsersPlugin configureUsersModule(PluginConfig pluginConfig, TokenGenerator tokenGenerator) {
        var eventSerializer = new JsonMapperEventSerializer(
                pluginConfig.getJsonMapper(),
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

        return new UsersPlugin(pluginConfig, usersConfig);
    }

    private static void checkIfAuthorized(Context ctx) {
        Set<Role> defaultRoles = Set.of(Role.AUTHORIZED); // We require authorization by default
        Set<RouteRole> permittedRoles = ctx.routeRoles();
        Set<? extends RouteRole> requiredRoles = permittedRoles.isEmpty() ? defaultRoles : permittedRoles;

        /*
        If the route is permitted for unauthorized use or the agent is a system agent, we allow the request through.
         */
        Agent agent = Auth.toAgent(ctx).block();
        if (requiredRoles.contains(UNAUTHORIZED) || agent.isSystem()) {
            return;
        }

        /*
        Here we only allow users that are authorized and thus must have a user ID!
         */
        boolean isUserAgent = agent.getUserId().isPresent();
        if (!isUserAgent) {
            throw new UnauthorizedResponse();
        }
    }

}
