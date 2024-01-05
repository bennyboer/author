package de.bennyboer.author.server.projects;

import de.bennyboer.author.auth.token.Token;
import de.bennyboer.author.auth.token.TokenContent;
import de.bennyboer.author.auth.token.TokenGenerator;
import de.bennyboer.author.auth.token.TokenVerifier;
import de.bennyboer.author.common.UserId;
import de.bennyboer.author.permissions.*;
import de.bennyboer.author.permissions.repo.InMemoryPermissionsRepo;
import de.bennyboer.author.permissions.repo.PermissionsRepo;
import de.bennyboer.author.project.Project;
import de.bennyboer.author.project.ProjectId;
import de.bennyboer.author.server.AppConfig;
import de.bennyboer.author.server.Profile;
import de.bennyboer.author.server.projects.api.ProjectDTO;
import de.bennyboer.author.server.projects.api.requests.CreateProjectRequest;
import de.bennyboer.author.server.projects.api.requests.RenameProjectRequest;
import de.bennyboer.author.server.projects.permissions.ProjectAction;
import de.bennyboer.author.server.projects.persistence.lookup.InMemoryProjectLookupRepo;
import de.bennyboer.author.server.projects.transformer.ProjectEventTransformer;
import de.bennyboer.author.server.shared.ModuleTest;
import de.bennyboer.author.server.shared.messaging.events.AggregateEventMessage;
import de.bennyboer.author.server.shared.persistence.JsonMapperEventSerializer;
import de.bennyboer.author.server.shared.persistence.RepoFactory;
import de.bennyboer.author.user.User;
import io.javalin.testtools.HttpClient;
import jakarta.annotation.Nullable;
import lombok.Value;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

public class ProjectsModuleTests extends ModuleTest {

    protected InMemoryProjectLookupRepo projectLookupRepo;
    protected PermissionsRepo permissionsRepo;

    protected final String correctToken = "correctToken";
    protected final String incorrectToken = "incorrectToken";
    protected final UserId userId = UserId.of("USER_ID");

    @Override
    protected AppConfig configure() {
        projectLookupRepo = new InMemoryProjectLookupRepo();
        permissionsRepo = new InMemoryPermissionsRepo();

        TokenGenerator tokenGenerator = content -> Mono.just(Token.of(correctToken));
        TokenVerifier tokenVerifier = token -> {
            if (token.getValue().equals(correctToken)) {
                return Mono.just(TokenContent.user(userId));
            }

            return Mono.error(new Exception("Invalid token"));
        };

        return AppConfig.builder()
                .profile(Profile.TESTING)
                .tokenGenerator(tokenGenerator)
                .tokenVerifier(tokenVerifier)
                .modules(List.of(
                        (moduleConfig) -> {
                            var eventSerializer = new JsonMapperEventSerializer(
                                    moduleConfig.getJsonMapper(),
                                    ProjectEventTransformer::toSerialized,
                                    ProjectEventTransformer::fromSerialized
                            );
                            var eventSourcingRepo = RepoFactory.createEventSourcingRepo(Project.TYPE, eventSerializer);

                            ProjectsConfig projectsConfig = ProjectsConfig.builder()
                                    .eventSourcingRepo(eventSourcingRepo)
                                    .permissionsRepo(permissionsRepo)
                                    .projectLookupRepo(projectLookupRepo)
                                    .build();

                            return new ProjectsModule(moduleConfig, projectsConfig);
                        }
                ))
                .build();
    }

    protected void userIsCreatedThatIsNotAllowedToCreateProjects() {
        // Do nothing
    }

    protected void userIsCreatedThatIsAllowedToCreateProjects() {
        AggregateEventMessage message = AggregateEventMessage.builder()
                .aggregateType(User.TYPE.getValue())
                .aggregateId(userId.getValue())
                .aggregateVersion(0L)
                .date(Instant.now())
                .eventName("CREATED")
                .eventVersion(0L)
                .build();
        publishAggregateEventMessage(message);

        awaitProjectPermissionsForUserGiven();
    }

    protected int renameProject(HttpClient client, String projectId, long version, String newName, String token) {
        RenameProjectRequest request = RenameProjectRequest.builder()
                .name(newName)
                .build();
        String requestJson = getJsonMapper().toJsonString(request, RenameProjectRequest.class);

        var response = client.post(
                "/api/projects/%s/rename?version=%d".formatted(projectId, version),
                requestJson,
                req -> req.header("Authorization", "Bearer " + token)
        );

        return response.code();
    }

    protected GetProjectTestResponse getProject(HttpClient client, String projectId, String token) throws IOException {
        var response = client.get(
                "/api/projects/%s".formatted(projectId),
                req -> req.header("Authorization", "Bearer " + token)
        );

        int statusCode = response.code();
        String responseJson = response.body().string();

        ProjectDTO project = null;
        if (statusCode == 200) {
            project = getJsonMapper().fromJsonString(responseJson, ProjectDTO.class);
        }

        return new GetProjectTestResponse(statusCode, project);
    }

    protected String createProjectAndAwaitCreation(HttpClient client, String name, String token) {
        var response = createProject(client, name, token);

        awaitProjectCreation(response.getProjectId());

        return response.getProjectId();
    }

    protected void awaitProjectCreation(String projectId) {
        ProjectId id = ProjectId.of(projectId);

        projectLookupRepo.awaitUpdate(id);
        awaitProjectPermissionsSetup(id);
    }

    protected int removeProject(HttpClient client, String projectId, long version, String token) {
        var response = client.delete(
                "/api/projects/%s?version=%d".formatted(projectId, version),
                null,
                req -> req.header("Authorization", "Bearer " + token)
        );

        return response.code();
    }

    protected CreateProjectTestResponse createProject(HttpClient client, String name, String token) {
        CreateProjectRequest request = CreateProjectRequest.builder()
                .name(name)
                .build();
        String requestJson = getJsonMapper().toJsonString(request, CreateProjectRequest.class);

        var response = client.post(
                "/api/projects",
                requestJson,
                req -> req.header("Authorization", "Bearer " + token)
        );

        int statusCode = response.code();

        String projectId = null;
        if (statusCode == 204) {
            projectId = response.header("Location").split("/")[3];
        }

        return new CreateProjectTestResponse(statusCode, projectId);
    }

    private void awaitProjectPermissionsSetup(ProjectId projectId) {
        Permission permission = Permission.builder()
                .user(userId)
                .isAllowedTo(Action.of(ProjectAction.READ.name()))
                .on(Resource.of(ResourceType.of(Project.TYPE.getValue()), ResourceId.of(projectId.getValue())));

        awaitPermissionCreation(permission, permissionsRepo);
    }

    private void awaitProjectPermissionsForUserGiven() {
        Permission permission = Permission.builder()
                .user(userId)
                .isAllowedTo(Action.of(ProjectAction.CREATE.name()))
                .on(Resource.ofType(ResourceType.of(Project.TYPE.getValue())));

        awaitPermissionCreation(permission, permissionsRepo);
    }

    @Value
    public static class CreateProjectTestResponse {

        int statusCode;

        @Nullable
        String projectId;

    }

    @Value
    public static class GetProjectTestResponse {

        int statusCode;

        @Nullable
        ProjectDTO project;

    }

}
