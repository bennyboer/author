package de.bennyboer.author.server;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import de.bennyboer.author.server.structure.facade.TreeFacade;
import de.bennyboer.author.server.structure.rest.StructureRestRouting;
import de.bennyboer.author.server.structure.rest.TreeRestHandler;
import de.bennyboer.author.server.websocket.WebSocketEventPublisher;
import de.bennyboer.author.server.websocket.WebSocketService;
import de.bennyboer.author.structure.tree.api.NodeName;
import de.bennyboer.author.structure.tree.api.TreeId;
import de.bennyboer.author.structure.tree.api.TreeIdAndVersion;
import de.bennyboer.author.structure.tree.api.TreeService;
import de.bennyboer.common.UserId;
import de.bennyboer.eventsourcing.api.persistence.InMemoryEventSourcingRepo;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;

import static io.javalin.apibuilder.ApiBuilder.path;

public class App {

    public static void main(String[] args) {
        var webSocketService = new WebSocketService();

        var eventSourcingRepo = new InMemoryEventSourcingRepo();
        var eventPublisher = new WebSocketEventPublisher(webSocketService); // TODO Use a messaging system instead

        var treeService = new TreeService(eventSourcingRepo, eventPublisher);

        // TODO For now that we do not have projects we need to create a tree here for testing purposes
        // TODO Remove when a tree is created as a side-effect of creating a project
        var testTreeId = treeService.create(NodeName.of("Root"), UserId.of("TEST_USER_ID"))
                .map(TreeIdAndVersion::getId)
                .map(TreeId::getValue)
                .block();
        System.out.println("Test tree ID: " + testTreeId);

        var treeFacade = new TreeFacade(treeService);

        var treeRestHandler = new TreeRestHandler(treeFacade);
        var structureRestRouting = new StructureRestRouting(treeRestHandler);

        Javalin.create(config -> {
                    config.plugins.enableCors(cors -> {
                        cors.add(it -> {
                            it.anyHost(); // TODO Restrict to frontend host and only allow for DEV build
                        });
                    });

                    config.jsonMapper(new JavalinJackson().updateMapper(mapper -> {
                        mapper.registerModule(new Jdk8Module());
                        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                    }));
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
                    });
                })
                .start(7070);
    }

}
