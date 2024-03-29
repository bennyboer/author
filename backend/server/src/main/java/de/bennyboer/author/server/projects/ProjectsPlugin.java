package de.bennyboer.author.server.projects;

import de.bennyboer.author.eventsourcing.aggregate.AggregateId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.author.project.Project;
import de.bennyboer.author.project.ProjectId;
import de.bennyboer.author.project.ProjectsService;
import de.bennyboer.author.server.projects.facade.ProjectsCommandFacade;
import de.bennyboer.author.server.projects.facade.ProjectsPermissionsFacade;
import de.bennyboer.author.server.projects.facade.ProjectsQueryFacade;
import de.bennyboer.author.server.projects.facade.ProjectsSyncFacade;
import de.bennyboer.author.server.projects.messaging.*;
import de.bennyboer.author.server.projects.permissions.ProjectPermissionsService;
import de.bennyboer.author.server.projects.rest.ProjectsRestHandler;
import de.bennyboer.author.server.projects.rest.ProjectsRestRouting;
import de.bennyboer.author.server.projects.transformer.ProjectEventTransformer;
import de.bennyboer.author.server.shared.messaging.events.AggregateEventMessageListener;
import de.bennyboer.author.server.shared.messaging.events.AggregateEventPayloadTransformer;
import de.bennyboer.author.server.shared.messaging.permissions.MessagingAggregatePermissionsEventPublisher;
import de.bennyboer.author.server.shared.modules.AppPlugin;
import de.bennyboer.author.server.shared.modules.PluginConfig;
import de.bennyboer.author.server.shared.websocket.subscriptions.events.AggregateEventPermissionChecker;
import io.javalin.config.JavalinConfig;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static io.javalin.apibuilder.ApiBuilder.path;

public class ProjectsPlugin extends AppPlugin {

    private final ProjectsQueryFacade queryFacade;

    private final ProjectsCommandFacade commandFacade;

    private final ProjectsPermissionsFacade permissionsFacade;

    private final ProjectsSyncFacade syncFacade;

    public ProjectsPlugin(PluginConfig config, ProjectsConfig projectsConfig) {
        super(config);

        var eventSourcingRepo = projectsConfig.getEventSourcingRepo();
        var projectsService = new ProjectsService(eventSourcingRepo, getEventPublisher());

        var permissionsRepo = projectsConfig.getPermissionsRepo();
        var permissionsEventPublisher = new MessagingAggregatePermissionsEventPublisher(
                config.getMessaging(),
                config.getJsonMapper()
        );
        var projectPermissionsService = new ProjectPermissionsService(permissionsRepo, permissionsEventPublisher);

        var lookupRepo = projectsConfig.getProjectLookupRepo();

        queryFacade = new ProjectsQueryFacade(projectsService, projectPermissionsService, lookupRepo);
        commandFacade = new ProjectsCommandFacade(projectsService, projectPermissionsService);
        permissionsFacade = new ProjectsPermissionsFacade(projectPermissionsService);
        syncFacade = new ProjectsSyncFacade(projectsService, lookupRepo);
    }

    @Override
    public void onStart(@NotNull JavalinConfig config) {
        var restHandler = new ProjectsRestHandler(queryFacade, commandFacade);
        var restRouting = new ProjectsRestRouting(restHandler);

        config.router.apiBuilder(() -> path("/api/projects", restRouting));
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
    protected Map<AggregateType, AggregateEventPayloadTransformer> getAggregateEventPayloadTransformers() {
        return Map.of(Project.TYPE, ProjectEventTransformer::toApi);
    }

}
