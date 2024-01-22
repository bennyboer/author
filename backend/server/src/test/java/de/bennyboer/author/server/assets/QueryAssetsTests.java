package de.bennyboer.author.server.assets;

import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class QueryAssetsTests extends AssetsModuleTests {

    @Test
    void shouldQueryAsset() {
        JavalinTest.test(getJavalin(), (server, client) -> {
            // given: a user is created that is allowed to create assets
            userIsCreatedThatIsAllowedToCreateAssets();

            // and: the user has an asset
            var assetId = createAssetAndAwaitCreation(client, "Test Asset", "text/plain", correctToken);

            // when: the asset is queried
            var response = getAsset(client, assetId, correctToken);

            // then: the status code is 200
            assertThat(response.getStatusCode()).isEqualTo(200);

            // and: the asset is returned
            var asset = response.getAsset();
            assertThat(asset.getId()).isEqualTo(assetId);
            assertThat(asset.getVersion()).isEqualTo(0L);
            assertThat(asset.getContent()).isEqualTo("Test Asset");
            assertThat(asset.getContentType()).isEqualTo("text/plain");
            assertThat(asset.getCreatedAt()).isNotNull();
        });
    }

    @Test
    void shouldNotBeAbleToQueryAssetGivenAnIncorrectToken() {
        JavalinTest.test(getJavalin(), (server, client) -> {
            // given: a user is created that is allowed to create assets
            userIsCreatedThatIsAllowedToCreateAssets();

            // and: the user has an asset
            var assetId = createAssetAndAwaitCreation(client, "Test Asset", "text/plain", correctToken);

            // when: the asset is queried with an incorrect token
            var response = getAsset(client, assetId, incorrectToken);

            // then: the status code is 401
            assertThat(response.getStatusCode()).isEqualTo(401);

            // and: the asset is not returned
            assertThat(response.getAsset()).isNull();
        });
    }

    @Test
    void shouldQueryAssetContent() {
        JavalinTest.test(getJavalin(), (server, client) -> {
            // given: a user is created that is allowed to create assets
            userIsCreatedThatIsAllowedToCreateAssets();

            // and: the user has an asset
            var assetId = createAssetAndAwaitCreation(client, "Test Asset", "text/plain", correctToken);

            // when: the asset content is queried
            var response = getAssetContent(client, assetId, correctToken);

            // then: the status code is 200
            assertThat(response.getStatusCode()).isEqualTo(200);

            // and: the asset content is returned
            assertThat(response.getContent()).isEqualTo("Test Asset");
            assertThat(response.getContentType()).isEqualTo("text/plain");
        });
    }

}
