package de.bennyboer.author.server.structure;

import de.bennyboer.author.auth.token.Token;
import de.bennyboer.author.auth.token.TokenContent;
import de.bennyboer.author.auth.token.TokenGenerator;
import de.bennyboer.author.auth.token.TokenVerifier;
import de.bennyboer.author.common.UserId;
import de.bennyboer.author.permissions.*;
import de.bennyboer.author.permissions.repo.PermissionsRepo;
import de.bennyboer.author.project.Project;
import de.bennyboer.author.project.ProjectEvent;
import de.bennyboer.author.server.AppConfig;
import de.bennyboer.author.server.shared.ModuleTest;
import de.bennyboer.author.server.shared.messaging.events.AggregateEventMessage;
import de.bennyboer.author.server.shared.persistence.JsonMapperEventSerializer;
import de.bennyboer.author.server.shared.persistence.RepoFactory;
import de.bennyboer.author.server.structure.api.StructureDTO;
import de.bennyboer.author.server.structure.api.requests.AddChildRequest;
import de.bennyboer.author.server.structure.external.project.ProjectDetailsService;
import de.bennyboer.author.server.structure.permissions.StructureAction;
import de.bennyboer.author.server.structure.persistence.lookup.InMemoryStructureLookupRepo;
import de.bennyboer.author.server.structure.transformer.StructureEventTransformer;
import de.bennyboer.author.structure.Structure;
import de.bennyboer.author.structure.StructureId;
import de.bennyboer.author.user.User;
import de.bennyboer.author.user.UserEvent;
import io.javalin.testtools.HttpClient;
import jakarta.annotation.Nullable;
import lombok.Value;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StructureModuleTests extends ModuleTest {

    protected InMemoryStructureLookupRepo structureLookupRepo;
    protected PermissionsRepo permissionsRepo;

    protected final String correctToken = "correctToken";
    protected final String incorrectToken = "incorrectToken";
    protected final UserId userId = UserId.of("USER_ID");
    protected final Map<String, String> projectNameLookupByProjectId = new HashMap<>();

    @Override
    protected AppConfig configure(AppConfig.AppConfigBuilder configBuilder) {
        structureLookupRepo = new InMemoryStructureLookupRepo();
        permissionsRepo = RepoFactory.createPermissionsRepo("structure");

        TokenGenerator tokenGenerator = content -> Mono.just(Token.of(correctToken));
        TokenVerifier tokenVerifier = token -> {
            if (token.getValue().equals(correctToken)) {
                return Mono.just(TokenContent.user(userId));
            }

            return Mono.error(new Exception("Invalid token"));
        };

        return configBuilder
                .tokenGenerator(tokenGenerator)
                .tokenVerifier(tokenVerifier)
                .modules(List.of(
                        (moduleConfig) -> {
                            var eventSerializer = new JsonMapperEventSerializer(
                                    moduleConfig.getJsonMapper(),
                                    StructureEventTransformer::toSerialized,
                                    StructureEventTransformer::fromSerialized
                            );
                            var eventSourcingRepo = RepoFactory.createEventSourcingRepo(
                                    Structure.TYPE,
                                    eventSerializer
                            );

                            var projectDetailsService = new ProjectDetailsService() {

                                @Override
                                public Mono<String> getProjectName(String projectId) {
                                    return Mono.justOrEmpty(projectNameLookupByProjectId.get(projectId));
                                }

                            };

                            StructureConfig structureConfig = StructureConfig.builder()
                                    .eventSourcingRepo(eventSourcingRepo)
                                    .permissionsRepo(permissionsRepo)
                                    .structureLookupRepo(structureLookupRepo)
                                    .projectDetailsService(projectDetailsService)
                                    .build();

                            return new StructureModule(moduleConfig, structureConfig);
                        }
                ))
                .build();
    }

    protected void setProjectNameByProjectId(String projectId, String projectName) {
        projectNameLookupByProjectId.put(projectId, projectName);
    }

    protected void projectHasBeenRemoved(String projectId) {
        AggregateEventMessage eventMessage = AggregateEventMessage.builder()
                .aggregateType(Project.TYPE.getValue())
                .aggregateId(projectId)
                .aggregateVersion(1L)
                .userId(userId.getValue())
                .date(Instant.now())
                .eventVersion(0L)
                .eventName(ProjectEvent.REMOVED.name())
                .build();

        publishAggregateEventMessage(eventMessage);
    }

    protected void awaitStructureToBeRemoved(String structureId) {
        structureLookupRepo.awaitRemoval(StructureId.of(structureId));
    }

    protected void projectAndItsCorrespondingStructureHaveBeenCreated(String projectId, String name) {
        projectHasBeenCreated(projectId, name);
        awaitStructureToBeCreatedForProject(projectId);
    }

    protected void projectHasBeenCreated(String projectId, String name) {
        setProjectNameByProjectId(projectId, name);

        AggregateEventMessage eventMessage = AggregateEventMessage.builder()
                .aggregateType(Project.TYPE.getValue())
                .aggregateId(projectId)
                .aggregateVersion(0L)
                .userId(userId.getValue())
                .date(Instant.now())
                .eventVersion(0L)
                .eventName(ProjectEvent.CREATED.name())
                .build();

        publishAggregateEventMessage(eventMessage);
    }

    protected void loggedInUserIsRemovedAndStructurePermissionsUpdated(String structureId) {
        userIsRemoved(userId.getValue());
        awaitUserPermissionsToBeRemoved(userId.getValue(), structureId);
    }

    protected void userIsRemoved(String userId) {
        AggregateEventMessage eventMessage = AggregateEventMessage.builder()
                .aggregateType(User.TYPE.getValue())
                .aggregateId(userId)
                .aggregateVersion(1L)
                .userId(userId)
                .date(Instant.now())
                .eventVersion(0L)
                .eventName(UserEvent.REMOVED.name())
                .build();

        publishAggregateEventMessage(eventMessage);
    }

    protected void awaitUserPermissionsToBeRemoved(String userId, String structureId) {
        Permission permission = Permission.builder()
                .user(UserId.of(userId))
                .isAllowedTo(Action.of(StructureAction.READ.name()))
                .on(Resource.of(ResourceType.of(Structure.TYPE.getValue()), ResourceId.of(structureId)));

        awaitPermissionRemoval(permission, permissionsRepo);
    }

    protected void awaitStructureToBeCreatedForProject(String projectId) {
        structureLookupRepo.awaitUpdate(s -> s.getProjectId().equals(projectId));
    }

    protected GetStructureIdByProjectIdTestResponse getStructureIdByProjectId(
            HttpClient client,
            String token,
            String projectId
    ) throws IOException {
        var response = client.get(
                "/api/structures/by-project-id/%s".formatted(projectId),
                req -> req.header("Authorization", "Bearer %s".formatted(token))
        );

        int statusCode = response.code();
        String structureId = null;
        if (statusCode == 200) {
            structureId = response.body().string();
        }

        return new GetStructureIdByProjectIdTestResponse(statusCode, structureId);
    }

    protected GetStructureTestResponse getStructure(HttpClient client, String token, String structureId) throws
            IOException {
        var response = client.get(
                "/api/structures/%s".formatted(structureId),
                req -> req.header("Authorization", "Bearer %s".formatted(token))
        );

        int statusCode = response.code();
        StructureDTO structure = null;
        if (statusCode == 200) {
            structure = getJsonMapper().fromJsonString(response.body().string(), StructureDTO.class);
        }

        return new GetStructureTestResponse(statusCode, structure);
    }

    protected int addNodeChild(
            HttpClient client,
            String token,
            String structureId,
            long version,
            String parentId,
            String name
    ) {
        AddChildRequest request = AddChildRequest.builder()
                .name(name)
                .build();
        String requestJson = getJsonMapper().toJsonString(request, AddChildRequest.class);

        var response = client.post(
                "/api/structures/%s/nodes/%s/add-child?version=%d".formatted(structureId, parentId, version),
                requestJson,
                req -> req.header("Authorization", "Bearer %s".formatted(token))
        );

        return response.code();
    }

    protected int toggleNode(
            HttpClient client,
            String token,
            String structureId,
            long version,
            String nodeId
    ) {
        var response = client.post(
                "/api/structures/%s/nodes/%s/toggle?version=%d".formatted(structureId, nodeId, version),
                null,
                req -> req.header("Authorization", "Bearer %s".formatted(token))
        );

        return response.code();
    }

    @Value
    public static class GetStructureIdByProjectIdTestResponse {

        int statusCode;

        @Nullable
        String structureId;

    }

    @Value
    public static class GetStructureTestResponse {

        int statusCode;

        @Nullable
        StructureDTO structure;

    }

}
