package de.bennyboer.author.assets;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.aggregate.AggregateId;
import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.author.eventsourcing.persistence.EventSourcingRepo;
import de.bennyboer.author.eventsourcing.persistence.InMemoryEventSourcingRepo;
import de.bennyboer.author.eventsourcing.testing.TestEventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AssetsServiceTests {

    private final EventSourcingRepo eventSourcingRepo = new InMemoryEventSourcingRepo();

    private final AssetsService assetsService = new AssetsService(
            eventSourcingRepo,
            new TestEventPublisher()
    );

    private final Agent testAgent = Agent.user(UserId.of("TEST_USER_ID"));

    @Test
    void shouldCreateAsset() {
        // given: the content of an asset to be created
        var content = Content.of("Test Asset", ContentType.fromMimeType("text/plain"));

        // when: an asset is created
        var assetIdAndVersion = assetsService.create(content, testAgent).block();
        var assetId = assetIdAndVersion.getId();
        var version = assetIdAndVersion.getVersion();

        // then: the asset can be retrieved
        var asset = assetsService.get(assetId, version).block();
        assertThat(asset.getVersion()).isEqualTo(Version.zero());
        assertThat(asset.getContent()).isEqualTo(Optional.of(content));
        assertThat(asset.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldNotAllowCreatingAssetForSystemAgent() {
        // given: the content of an asset to be created
        var content = Content.of("Test Asset", ContentType.fromMimeType("text/plain"));

        // when: an asset is created with system agent
        Executable executable = () -> assetsService.create(content, Agent.system()).block();

        // then: an exception is thrown
        var exception = assertThrows(
                IllegalArgumentException.class,
                executable
        );
        assertEquals(
                "System agent is not allowed to create assets",
                exception.getMessage()
        );
    }

    @Test
    void shouldNotAllowCreatingAssetForAnonymousAgent() {
        // given: the content of an asset to be created
        var content = Content.of("Test Asset", ContentType.fromMimeType("text/plain"));

        // when: an asset is created with anonymous agent
        Executable executable = () -> assetsService.create(content, Agent.anonymous()).block();

        // then: an exception is thrown
        var exception = assertThrows(
                IllegalArgumentException.class,
                executable
        );
        assertEquals(
                "Anonymous agent is not allowed to create assets",
                exception.getMessage()
        );
    }

    @Test
    void shouldRemoveAsset() {
        // given: an asset
        var content = Content.of("Test Asset", ContentType.fromMimeType("text/plain"));
        var assetIdAndVersion = assetsService.create(content, testAgent).block();
        var assetId = assetIdAndVersion.getId();
        var initialVersion = assetIdAndVersion.getVersion();

        // when: the asset is removed
        var version = assetsService.remove(assetId, initialVersion, testAgent).block();

        // then: the asset is gone
        var asset = assetsService.get(assetId, version).block();
        assertThat(asset).isNull();

        // and: the events are collapsed and the content is gone for good
        var events = eventSourcingRepo.findEventsByAggregateIdAndType(
                AggregateId.of(assetId.getValue()),
                Asset.TYPE,
                Version.zero()
        ).collectList().block();
        assertEquals(1, events.size());
        var event = events.stream().findFirst().orElseThrow();
        var aggregate = Asset.init();
        aggregate = aggregate.apply(event.getEvent(), event.getMetadata());
        assertThat(aggregate.getContent().isPresent()).isFalse();
    }

    @Test
    void shouldNotAcceptOtherCommandBeforeCreating() {
        // when: trying to remove a non-existing asset
        Executable executable = () -> assetsService.remove(
                AssetId.create(),
                Version.zero(),
                testAgent
        ).block();

        // then: an exception is thrown
        var exception = assertThrows(
                IllegalStateException.class,
                executable
        );
        assertEquals(
                "Asset must be initialized with CreateCmd before applying other commands",
                exception.getMessage()
        );
    }

    @Test
    void shouldNotAcceptCommandsAfterRemoval() {
        // given: an removed asset
        var content = Content.of("Test Asset", ContentType.fromMimeType("text/plain"));
        var assetIdAndVersion = assetsService.create(content, testAgent).block();
        var assetId = assetIdAndVersion.getId();
        var initialVersion = assetIdAndVersion.getVersion();
        var version = assetsService.remove(assetId, initialVersion, testAgent).block();

        // when: trying to remove the removed project
        Executable executable = () -> assetsService.remove(
                assetId,
                version,
                testAgent
        ).block();

        // then: an exception is thrown
        var exception = assertThrows(
                IllegalStateException.class,
                executable
        );
        assertEquals(
                "Cannot apply command to removed Asset",
                exception.getMessage()
        );
    }

}
