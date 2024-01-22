package de.bennyboer.author.server.assets;

import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateAssetTests extends AssetsModuleTests {

    @Test
    void shouldCreateAsset() {
        JavalinTest.test(getJavalin(), (server, client) -> {
            // given: a user is created that is allowed to create assets
            userIsCreatedThatIsAllowedToCreateAssets();

            // when: creating an asset with a correct token
            var response = createAsset(client, "Test Asset", "text/plain", correctToken);

            // then: the server responds with 204
            assertThat(response.getStatusCode()).isEqualTo(204);

            // and: with the ID of the created asset
            assertThat(response.getAssetId()).isNotNull();

            // when: getting the asset
            var getAssetResponse = getAsset(client, response.getAssetId(), correctToken);

            // then: the asset is returned
            assertThat(getAssetResponse.getStatusCode()).isEqualTo(200);
            var asset = getAssetResponse.getAsset();
            assertThat(asset.getId()).isEqualTo(response.getAssetId());
            assertThat(asset.getVersion()).isEqualTo(0L);
            assertThat(asset.getContent()).isEqualTo("Test Asset");
            assertThat(asset.getContentType()).isEqualTo("text/plain");
            assertThat(asset.getCreatedAt()).isNotNull();
        });
    }

    @Test
    void shouldNotBeAbleToCreateAssetGivenAnIncorrectToken() {
        JavalinTest.test(getJavalin(), (server, client) -> {
            // given: a user is created that is allowed to create assets
            userIsCreatedThatIsAllowedToCreateAssets();

            // when: creating an asset with an incorrect token
            var response = createAsset(client, "Test Asset", "text/plain", incorrectToken);

            // then: the server responds with 401
            assertThat(response.getStatusCode()).isEqualTo(401);

            // and: no asset is created
            var getAssetResponse = getAsset(client, response.getAssetId(), correctToken);
            assertThat(getAssetResponse.getStatusCode()).isEqualTo(403);
        });
    }

}
