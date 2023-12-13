package de.bennyboer.author.server;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import de.bennyboer.author.project.Project;
import de.bennyboer.author.project.ProjectService;
import de.bennyboer.author.server.project.facade.ProjectFacade;
import de.bennyboer.author.server.project.rest.ProjectRestHandler;
import de.bennyboer.author.server.project.rest.ProjectRestRouting;
import de.bennyboer.author.server.project.transformer.ProjectEventTransformer;
import de.bennyboer.author.server.shared.messaging.Messaging;
import de.bennyboer.author.server.shared.messaging.MessagingEventPublisher;
import de.bennyboer.author.server.shared.websocket.WebSocketService;
import de.bennyboer.author.server.structure.facade.TreeFacade;
import de.bennyboer.author.server.structure.rest.StructureRestRouting;
import de.bennyboer.author.server.structure.rest.TreeRestHandler;
import de.bennyboer.author.server.structure.transformer.TreeEventTransformer;
import de.bennyboer.author.server.user.facade.UserFacade;
import de.bennyboer.author.server.user.persistence.lookup.UserLookupInMemoryRepo;
import de.bennyboer.author.server.user.rest.UserRestHandler;
import de.bennyboer.author.server.user.rest.UserRestRouting;
import de.bennyboer.author.server.user.transformer.UserEventTransformer;
import de.bennyboer.author.structure.tree.Tree;
import de.bennyboer.author.structure.tree.TreeId;
import de.bennyboer.author.structure.tree.TreeService;
import de.bennyboer.author.structure.tree.node.NodeName;
import de.bennyboer.author.user.Password;
import de.bennyboer.author.user.User;
import de.bennyboer.author.user.UserName;
import de.bennyboer.author.user.UserService;
import de.bennyboer.common.UserId;
import de.bennyboer.eventsourcing.aggregate.AggregateIdAndVersion;
import de.bennyboer.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.eventsourcing.persistence.InMemoryEventSourcingRepo;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;
import io.javalin.json.JsonMapper;
import io.javalin.plugin.bundled.CorsPluginConfig;

import static io.javalin.apibuilder.ApiBuilder.path;

public class App {

    public static void main(String[] args) {
        JsonMapper jsonMapper = new JavalinJackson().updateMapper(mapper -> {
            mapper.registerModule(new Jdk8Module());
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        });

        var messaging = new Messaging();
        messaging.registerAggregateType(User.TYPE);
        messaging.registerAggregateType(Project.TYPE);
        messaging.registerAggregateType(Tree.TYPE);

        var eventSourcingRepo = new InMemoryEventSourcingRepo();
        var eventPublisher = new MessagingEventPublisher(messaging, jsonMapper);
        eventPublisher.registerAggregateEventPayloadTransformer(User.TYPE, UserEventTransformer::toApi);
        eventPublisher.registerAggregateEventPayloadTransformer(Project.TYPE, ProjectEventTransformer::toApi);
        eventPublisher.registerAggregateEventPayloadTransformer(Tree.TYPE, TreeEventTransformer::toApi);

        var webSocketService = new WebSocketService(messaging, jsonMapper);

        // TODO Authentication module with login (JWT)

        var userService = new UserService(eventSourcingRepo, eventPublisher);
        var userLookupRepo = new UserLookupInMemoryRepo();
        var userFacade = new UserFacade(userService, userLookupRepo);
        var userRestHandler = new UserRestHandler(userFacade);
        var userRestRouting = new UserRestRouting(userRestHandler);

        var projectService = new ProjectService(eventSourcingRepo, eventPublisher);
        var projectFacade = new ProjectFacade(projectService);
        var projectRestHandler = new ProjectRestHandler(projectFacade);
        var projectRestRouting = new ProjectRestRouting(projectRestHandler);

        var treeService = new TreeService(eventSourcingRepo, eventPublisher);
        var treeFacade = new TreeFacade(treeService);
        var treeRestHandler = new TreeRestHandler(treeFacade);
        var structureRestRouting = new StructureRestRouting(treeRestHandler);

        Javalin.create(config -> {
                    config.plugins.enableCors(cors -> {
                        // TODO Restrict to frontend host and only allow for DEV build
                        cors.add(CorsPluginConfig::anyHost);
                    });

                    config.jsonMapper(jsonMapper);
                })
                .get("/", ctx -> ctx.result("Hello World")) // TODO Maybe serve frontend here?
                .ws("/ws", ws -> {
                    ws.onConnect(webSocketService::onConnect);
                    ws.onClose(webSocketService::onClose);
                    ws.onError(webSocketService::onError);
                    ws.onMessage(webSocketService::onMessage);
                })
                .routes(() -> {
                    path("/api", () -> {
                        path("/structure", structureRestRouting);
                        path("/projects", projectRestRouting);
                        path("/users", userRestRouting);
                    });
                })
                .events(event -> {
                    event.serverStarted(() -> {
                        // TODO For now we create a test user here for testing purposes
                        // TODO This is later to be replaced by some configuration and a signup mechanism
                        var testUserId = userService.create(UserName.of("test"), Password.of("test"), Agent.system())
                                .map(AggregateIdAndVersion::getId)
                                .map(UserId::getValue)
                                .block();
                        System.out.printf("### TEST USER ID '%s' ###%n", testUserId);

                        // TODO For now that we do not have projects we need to create a tree here for testing purposes
                        // TODO Remove when a tree is created as a side-effect of creating a project
                        var testTreeId = treeService.create(NodeName.of("Root"), Agent.system())
                                .map(AggregateIdAndVersion::getId)
                                .map(TreeId::getValue)
                                .block();
                        System.out.println("Test tree ID: " + testTreeId);
                    });
                    event.serverStopping(messaging::stop);
                })
                .start(7070);
    }

}
