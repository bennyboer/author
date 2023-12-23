package de.bennyboer.author.server.projects;

import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.eventsourcing.persistence.InMemoryEventSourcingRepo;
import de.bennyboer.author.project.Project;
import de.bennyboer.author.project.ProjectsService;
import de.bennyboer.author.server.projects.facade.ProjectsFacade;
import de.bennyboer.author.server.projects.rest.ProjectsRestHandler;
import de.bennyboer.author.server.projects.rest.ProjectsRestRouting;
import de.bennyboer.author.server.projects.transformer.ProjectEventTransformer;
import de.bennyboer.author.server.shared.messaging.AggregateEventMessageListener;
import de.bennyboer.author.server.shared.messaging.AggregateEventPayloadTransformer;
import de.bennyboer.author.server.shared.modules.Module;
import de.bennyboer.author.server.shared.modules.ModuleConfig;
import io.javalin.Javalin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

import static io.javalin.apibuilder.ApiBuilder.path;

public class ProjectsModule extends Module {

    private final ProjectsFacade facade;

    public ProjectsModule(ModuleConfig config) {
        super(config);

        var eventSourcingRepo = new InMemoryEventSourcingRepo(); // TODO Use persistent repo
        var projectsService = new ProjectsService(eventSourcingRepo, getEventPublisher());

        facade = new ProjectsFacade(projectsService);
    }

    @Override
    public void apply(@NotNull Javalin javalin) {
        var restHandler = new ProjectsRestHandler(facade);
        var restRouting = new ProjectsRestRouting(restHandler);

        javalin.routes(() -> path("/api/projects", restRouting));
    }

    @Override
    protected List<AggregateEventMessageListener> createMessageListeners() {
        return List.of();
    }

    @Override
    protected List<AggregateType> getAggregateTypes() {
        return List.of(Project.TYPE);
    }

    @Override
    protected Map<AggregateType, AggregateEventPayloadTransformer> getAggregateEventPayloadTransformers() {
        return Map.of(Project.TYPE, ProjectEventTransformer::toApi);
    }

}
