package de.bennyboer.author.server.assets;

import de.bennyboer.author.assets.Asset;
import de.bennyboer.author.assets.AssetId;
import de.bennyboer.author.auth.token.Token;
import de.bennyboer.author.auth.token.TokenContent;
import de.bennyboer.author.auth.token.TokenGenerator;
import de.bennyboer.author.auth.token.TokenVerifier;
import de.bennyboer.author.common.UserId;
import de.bennyboer.author.permissions.*;
import de.bennyboer.author.permissions.repo.PermissionsRepo;
import de.bennyboer.author.server.AppConfig;
import de.bennyboer.author.server.assets.api.AssetDTO;
import de.bennyboer.author.server.assets.api.requests.CreateAssetRequest;
import de.bennyboer.author.server.assets.permissions.AssetAction;
import de.bennyboer.author.server.assets.persistence.lookup.InMemoryAssetLookupRepo;
import de.bennyboer.author.server.assets.transformer.AssetEventTransformer;
import de.bennyboer.author.server.shared.ModuleTest;
import de.bennyboer.author.server.shared.messaging.events.AggregateEventMessage;
import de.bennyboer.author.server.shared.persistence.JsonMapperEventSerializer;
import de.bennyboer.author.server.shared.persistence.RepoFactory;
import de.bennyboer.author.user.User;
import de.bennyboer.author.user.UserEvent;
import io.javalin.testtools.HttpClient;
import jakarta.annotation.Nullable;
import lombok.Value;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

public class AssetsModuleTests extends ModuleTest {

    protected InMemoryAssetLookupRepo assetLookupRepo;

    protected PermissionsRepo permissionsRepo;

    protected final String correctToken = "correctToken";

    protected final String incorrectToken = "incorrectToken";

    protected final UserId userId = UserId.of("USER_ID");

    @Override
    protected AppConfig configure(AppConfig.AppConfigBuilder configBuilder) {
        assetLookupRepo = new InMemoryAssetLookupRepo();
        permissionsRepo = RepoFactory.createPermissionsRepo("assets");

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
                                    AssetEventTransformer::toSerialized,
                                    AssetEventTransformer::fromSerialized
                            );
                            var eventSourcingRepo = RepoFactory.createEventSourcingRepo(Asset.TYPE, eventSerializer);

                            AssetsConfig assetsConfig = AssetsConfig.builder()
                                    .eventSourcingRepo(eventSourcingRepo)
                                    .permissionsRepo(permissionsRepo)
                                    .assetLookupRepo(assetLookupRepo)
                                    .build();

                            return new AssetsModule(moduleConfig, assetsConfig);
                        }
                ))
                .build();
    }

    protected void userIsCreatedThatIsNotAllowedToCreateAssets() {
        // Do nothing
    }

    protected void userIsCreatedThatIsAllowedToCreateAssets() {
        AggregateEventMessage message = AggregateEventMessage.builder()
                .aggregateType(User.TYPE.getValue())
                .aggregateId(userId.getValue())
                .aggregateVersion(0L)
                .date(Instant.now())
                .eventName("CREATED")
                .eventVersion(0L)
                .build();
        publishAggregateEventMessage(message);

        awaitAssetPermissionsForUserGiven();
    }

    protected void userHasBeenRemoved(UserId userId) {
        AggregateEventMessage eventMessage = AggregateEventMessage.builder()
                .aggregateType(User.TYPE.getValue())
                .aggregateId(userId.getValue())
                .aggregateVersion(1L)
                .userId(userId.getValue())
                .date(Instant.now())
                .eventVersion(0L)
                .eventName(UserEvent.REMOVED.name())
                .build();

        publishAggregateEventMessage(eventMessage);
    }

    protected void awaitAssetToBeRemoved(String assetId) {
        assetLookupRepo.awaitRemoval(AssetId.of(assetId));
    }

    protected GetAssetTestResponse getAsset(HttpClient client, String assetId, String token) throws IOException {
        var response = client.get(
                "/api/assets/" + assetId,
                req -> req.header("Authorization", "Bearer " + token)
        );

        int statusCode = response.code();
        String responseJson = response.body().string();

        AssetDTO asset = null;
        if (statusCode == 200) {
            asset = getJsonMapper().fromJsonString(responseJson, AssetDTO.class);
        }

        return new GetAssetTestResponse(statusCode, asset);
    }

    protected GetAssetContentTestResponse getAssetContent(HttpClient client, String assetId, String token) throws
            IOException {
        var response = client.get(
                "/api/assets/" + assetId + "/content",
                req -> req.header("Authorization", "Bearer " + token)
        );

        int statusCode = response.code();

        String content = null;
        String contentType = null;
        if (statusCode == 200) {
            contentType = response.header("Content-Type");
            content = response.body().string();
        }

        return new GetAssetContentTestResponse(statusCode, content, contentType);
    }

    protected RemoveAssetTestResponse removeAsset(HttpClient client, String assetId, long version, String token) {
        var response = client.delete(
                "/api/assets/" + assetId + "?version=" + version,
                null,
                req -> req.header("Authorization", "Bearer " + token)
        );

        int statusCode = response.code();

        return new RemoveAssetTestResponse(statusCode);
    }

    protected CreateAssetTestResponse createAsset(
            HttpClient client,
            String content,
            String contentType,
            String token
    ) {
        CreateAssetRequest request = CreateAssetRequest.builder()
                .content(content)
                .contentType(contentType)
                .build();
        String requestJson = getJsonMapper().toJsonString(request, CreateAssetRequest.class);

        var response = client.post(
                "/api/assets",
                requestJson,
                req -> req.header("Authorization", "Bearer " + token)
        );

        int statusCode = response.code();

        String assetId = null;
        if (statusCode == 204) {
            assetId = response.header("Location").split("/")[3];
        }

        return new CreateAssetTestResponse(statusCode, assetId);
    }

    protected String createAssetAndAwaitCreation(HttpClient client, String content, String contentType, String token) {
        var response = createAsset(client, content, contentType, token);

        awaitAssetCreation(response.getAssetId());

        return response.getAssetId();
    }

    protected void awaitAssetCreation(String assetId) {
        AssetId id = AssetId.of(assetId);

        assetLookupRepo.awaitUpdate(id);
        awaitAssetPermissionsSetup(id);
    }

    private void awaitAssetPermissionsSetup(AssetId assetId) {
        Permission permission = Permission.builder()
                .user(userId)
                .isAllowedTo(Action.of(AssetAction.READ.name()))
                .on(Resource.of(ResourceType.of(Asset.TYPE.getValue()), ResourceId.of(assetId.getValue())));

        awaitPermissionCreation(permission, permissionsRepo);
    }

    private void awaitAssetPermissionsForUserGiven() {
        Permission permission = Permission.builder()
                .user(userId)
                .isAllowedTo(Action.of(AssetAction.CREATE.name()))
                .on(Resource.ofType(ResourceType.of(Asset.TYPE.getValue())));

        awaitPermissionCreation(permission, permissionsRepo);
    }

    @Value
    public static class CreateAssetTestResponse {

        int statusCode;

        @Nullable
        String assetId;

    }

    @Value
    public static class GetAssetTestResponse {

        int statusCode;

        @Nullable
        AssetDTO asset;

    }

    @Value
    public static class GetAssetContentTestResponse {

        int statusCode;

        @Nullable
        String content;

        @Nullable
        String contentType;

    }

    @Value
    public static class RemoveAssetTestResponse {

        int statusCode;

    }

}
