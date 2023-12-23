package de.bennyboer.author.server.structure;

import de.bennyboer.author.server.shared.messaging.AggregateEventMessageListener;
import de.bennyboer.author.server.shared.messaging.AggregateEventPayloadTransformer;
import de.bennyboer.author.server.shared.modules.Module;
import de.bennyboer.author.server.shared.modules.ModuleConfig;
import de.bennyboer.author.server.structure.facade.TreeFacade;
import de.bennyboer.author.server.structure.rest.StructureRestRouting;
import de.bennyboer.author.server.structure.rest.TreeRestHandler;
import de.bennyboer.author.server.structure.transformer.TreeEventTransformer;
import de.bennyboer.author.structure.tree.Tree;
import de.bennyboer.author.structure.tree.TreeService;
import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import io.javalin.Javalin;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static io.javalin.apibuilder.ApiBuilder.path;

public class StructureModule extends Module {

    private final TreeFacade facade;

    public StructureModule(ModuleConfig config) {
        super(config);

        var treeService = new TreeService(config.getEventSourcingRepo(), config.getEventPublisher());
        facade = new TreeFacade(treeService);
    }

    @Override
    public void apply(@NotNull Javalin javalin) {
        var restHandler = new TreeRestHandler(facade);
        var restRouting = new StructureRestRouting(restHandler);

        javalin.routes(() -> path("/api/structure", restRouting));
    }

    @Override
    protected List<AggregateEventMessageListener> createMessageListeners() {
        return List.of(); // TODO Add Message listener to create a new tree when a project is created
    }

    @Override
    protected List<AggregateType> getAggregateTypes() {
        return List.of(Tree.TYPE);
    }

    @Override
    protected Map<AggregateType, AggregateEventPayloadTransformer> getAggregateEventPayloadTransformers() {
        return Map.of(Tree.TYPE, TreeEventTransformer::toApi);
    }

    @Override
    protected Mono<Void> onServerStarted() {
        // TODO Remove once we have a message listener that created a tree for a new project
        return facade.initSampleTree();
    }

}
