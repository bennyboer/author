package de.bennyboer.author.server.projects;

import de.bennyboer.author.eventsourcing.aggregate.AggregateId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.author.eventsourcing.persistence.InMemoryEventSourcingRepo;
import de.bennyboer.author.permissions.repo.InMemoryPermissionsRepo;
import de.bennyboer.author.project.Project;
import de.bennyboer.author.project.ProjectId;
import de.bennyboer.author.project.ProjectsService;
import de.bennyboer.author.server.projects.facade.ProjectsCommandFacade;
import de.bennyboer.author.server.projects.facade.ProjectsPermissionsFacade;
import de.bennyboer.author.server.projects.facade.ProjectsQueryFacade;
import de.bennyboer.author.server.projects.facade.ProjectsSyncFacade;
import de.bennyboer.author.server.projects.messaging.*;
import de.bennyboer.author.server.projects.permissions.ProjectPermissionsService;
import de.bennyboer.author.server.projects.persistence.lookup.InMemoryProjectLookupRepo;
import de.bennyboer.author.server.projects.rest.ProjectsRestHandler;
import de.bennyboer.author.server.projects.rest.ProjectsRestRouting;
import de.bennyboer.author.server.projects.transformer.ProjectEventTransformer;
import de.bennyboer.author.server.shared.messaging.events.AggregateEventMessageListener;
import de.bennyboer.author.server.shared.messaging.events.AggregateEventPayloadTransformer;
import de.bennyboer.author.server.shared.messaging.permissions.MessagingAggregatePermissionsEventPublisher;
import de.bennyboer.author.server.shared.modules.Module;
import de.bennyboer.author.server.shared.modules.ModuleConfig;
import de.bennyboer.author.server.shared.websocket.subscriptions.events.AggregateEventPermissionChecker;
import io.javalin.Javalin;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static io.javalin.apibuilder.ApiBuilder.path;

public class ProjectsModule extends Module {

    private final ProjectsQueryFacade queryFacade;

    private final ProjectsCommandFacade commandFacade;

    private final ProjectsPermissionsFacade permissionsFacade;

    private final ProjectsSyncFacade syncFacade;

    public ProjectsModule(ModuleConfig config) {
        super(config);

        var eventSourcingRepo = new InMemoryEventSourcingRepo(); // TODO Use persistent repo
        var projectsService = new ProjectsService(eventSourcingRepo, getEventPublisher());

        var permissionsRepo = new InMemoryPermissionsRepo(); // TODO Use persistent repo
        var permissionsEventPublisher = new MessagingAggregatePermissionsEventPublisher(
                config.getMessaging(),
                config.getJsonMapper()
        );
        var projectPermissionsService = new ProjectPermissionsService(permissionsRepo, permissionsEventPublisher);

        var lookupRepo = new InMemoryProjectLookupRepo(); // TODO Use persistent repo

        queryFacade = new ProjectsQueryFacade(projectsService, projectPermissionsService, lookupRepo);
        commandFacade = new ProjectsCommandFacade(projectsService, projectPermissionsService);
        permissionsFacade = new ProjectsPermissionsFacade(projectPermissionsService);
        syncFacade = new ProjectsSyncFacade(projectsService, lookupRepo);
    }

    @Override
    public void apply(Javalin javalin) {
        var restHandler = new ProjectsRestHandler(queryFacade, commandFacade);
        var restRouting = new ProjectsRestRouting(restHandler);

        javalin.routes(() -> path("/api/projects", restRouting));
    }

    @Override
    protected List<AggregateEventMessageListener> createMessageListeners() {
        return List.of(
                new UserCreatedAddPermissionToCreateProjectsMsgListener(permissionsFacade),
                new UserRemovedRemovePermissionsMsgListener(permissionsFacade),
                new ProjectCreatedAddPermissionForCreatorMsgListener(permissionsFacade),
                new ProjectRemovedRemovePermissionsMsgListener(permissionsFacade),
                new ProjectCreatedAddToLookupMsgListener(syncFacade),
                new ProjectRenamedUpdateInLookupMsgListener(syncFacade),
                new ProjectRemovedRemoveFromLookupMsgListener(syncFacade)
        );
    }

    @Override
    protected List<AggregateEventPermissionChecker> getEventPermissionCheckers() {
        return List.of(
                new AggregateEventPermissionChecker() {
                    @Override
                    public AggregateType getAggregateType() {
                        return Project.TYPE;
                    }

                    @Override
                    public Mono<Boolean> hasPermissionToReceiveEvents(Agent agent, AggregateId aggregateId) {
                        return permissionsFacade.hasPermissionToReceiveEvents(
                                agent,
                                ProjectId.of(aggregateId.getValue())
                        );
                    }
                }
        );
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
