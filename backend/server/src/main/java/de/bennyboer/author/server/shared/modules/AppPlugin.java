package de.bennyboer.author.server.shared.modules;

import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.server.shared.messaging.events.AggregateEventMessageListener;
import de.bennyboer.author.server.shared.messaging.events.AggregateEventPayloadTransformer;
import de.bennyboer.author.server.shared.messaging.events.MessagingEventPublisher;
import de.bennyboer.author.server.shared.websocket.subscriptions.events.AggregateEventPermissionChecker;
import io.javalin.config.JavalinConfig;
import io.javalin.plugin.Plugin;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Getter
public abstract class AppPlugin extends Plugin<Void> {

    private final PluginConfig config;

    private final MessagingEventPublisher eventPublisher;

    protected AppPlugin(PluginConfig config) {
        this.config = config;
        this.eventPublisher = new MessagingEventPublisher(config.getMessaging(), config.getJsonMapper());
    }

    @Override
    public void onInitialize(@NotNull JavalinConfig config) {
        this.initializeModule();

        config.events(event -> {
            event.serverStarted(() -> onServerStarted().block());
            event.serverStopped(() -> onServerStopped().block());
        });
    }

    protected Mono<Void> onServerStarted() {
        return Mono.empty();
    }

    protected Mono<Void> onServerStopped() {
        return Mono.empty();
    }

    protected abstract Map<AggregateType, AggregateEventPayloadTransformer> getAggregateEventPayloadTransformers();

    protected abstract List<AggregateEventMessageListener> createMessageListeners();

    protected abstract List<AggregateEventPermissionChecker> getEventPermissionCheckers();

    private void initializeModule() {
        registerAggregateEventPayloadTransformers();
        registerMessageListeners();
        registerWebSocketPermissions();
    }

    private void registerWebSocketPermissions() {
        for (var permissionChecker : getEventPermissionCheckers()) {
            config.getWebSocketService().registerSubscriptionPermissionChecker(permissionChecker);
        }
    }

    private void registerAggregateEventPayloadTransformers() {
        for (var entry : getAggregateEventPayloadTransformers().entrySet()) {
            eventPublisher.registerAggregateEventPayloadTransformer(entry.getKey(), entry.getValue());
        }
    }

    private void registerMessageListeners() {
        for (var listener : createMessageListeners()) {
            config.getMessaging().registerAggregateEventMessageListener(listener);
        }
    }

}
