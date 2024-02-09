package de.bennyboer.author.server.assets;

import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RemoveAssetTests extends AssetsPluginTests {

    @Test
    void shouldRemoveAsset() {
        JavalinTest.test(getJavalin(), (server, client) -> {
            // given: a user is created that is allowed to create assets
            userIsCreatedThatIsAllowedToCreateAssets();

            // and: the user has an asset
            var assetId = createAssetAndAwaitCreation(client, "Test Asset", "text/plain", correctToken);

            // when: the asset is removed
            var response = removeAsset(client, assetId, 0L, correctToken);

            // then: the status code is 204
            assertThat(response.getStatusCode()).isEqualTo(204);

            // and: the asset cannot be queried with the correct token
            var getAssetResponse = getAsset(client, correctToken, assetId);
            assertThat(getAssetResponse.getAsset()).isNull();
        });
    }

    @Test
    void shouldNotBeAbleToRemoveAssetWithoutValidToken() {
        JavalinTest.test(getJavalin(), (server, client) -> {
            // given: a user is created that is allowed to create assets
            userIsCreatedThatIsAllowedToCreateAssets();

            // and: the user has an asset
            var assetId = createAssetAndAwaitCreation(client, "Test Asset", "text/plain", correctToken);

            // when: the asset is removed with an invalid token
            var response = removeAsset(client, assetId, 0L, incorrectToken);

            // then: the status code is 401
            assertThat(response.getStatusCode()).isEqualTo(401);

            // and: the asset can still be queried with the correct token
            var getAssetResponse = getAsset(client, assetId, correctToken);
            assertThat(getAssetResponse.getAsset()).isNotNull();
        });
    }

    @Test
    void shouldRemoveAssetsWhenOwnerIsRemoved() {
        JavalinTest.test(getJavalin(), (server, client) -> {
            // given: a user is created that is allowed to create assets
            userIsCreatedThatIsAllowedToCreateAssets();

            // and: the user has multiple assets
            var assetId1 = createAssetAndAwaitCreation(client, "Test Asset 1", "text/plain", correctToken);
            var assetId2 = createAssetAndAwaitCreation(client, "Test Asset 2", "text/plain", correctToken);
            var assetId3 = createAssetAndAwaitCreation(client, "Test Asset 3", "text/plain", correctToken);

            // when: the user has been removed
            userHasBeenRemoved(userId);

            // and: we wait for the assets to be removed
            awaitAssetToBeRemoved(assetId1);
            awaitAssetToBeRemoved(assetId2);
            awaitAssetToBeRemoved(assetId3);

            // then: the assets cannot be queried with the correct token
            var response1 = getAsset(client, correctToken, assetId1);
            var response2 = getAsset(client, correctToken, assetId2);
            var response3 = getAsset(client, correctToken, assetId3);

            assertThat(response1.getAsset()).isNull();
            assertThat(response2.getAsset()).isNull();
            assertThat(response3.getAsset()).isNull();
        });
    }

}
