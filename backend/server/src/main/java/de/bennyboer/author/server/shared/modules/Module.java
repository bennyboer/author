package de.bennyboer.author.server.shared.modules;

import de.bennyboer.author.server.shared.messaging.AggregateEventMessageListener;
import de.bennyboer.author.server.shared.messaging.AggregateEventPayloadTransformer;
import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import io.javalin.Javalin;
import io.javalin.plugin.Plugin;
import io.javalin.plugin.PluginLifecycleInit;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public abstract class Module implements Plugin, PluginLifecycleInit {

    private final ModuleConfig config;

    protected Module(ModuleConfig config) {
        this.config = config;
    }

    @Override
    public void init(@NotNull Javalin javalin) {
        this.initializeModule();
        javalin.events(event -> event.serverStarted(() -> onServerStarted().block()));
    }

    protected Mono<Void> onServerStarted() {
        return Mono.empty();
    }

    protected abstract List<AggregateType> getAggregateTypes();

    protected abstract Map<AggregateType, AggregateEventPayloadTransformer> getAggregateEventPayloadTransformers();

    protected abstract List<AggregateEventMessageListener> createMessageListeners();

    private void initializeModule() {
        registerAggregatesWithMessaging();
        registerAggregateEventPayloadTransformers();
        registerMessageListeners();
    }

    private void registerAggregatesWithMessaging() {
        for (var aggregateType : getAggregateTypes()) {
            config.getMessaging().registerAggregateType(aggregateType);
        }
    }

    private void registerAggregateEventPayloadTransformers() {
        for (var entry : getAggregateEventPayloadTransformers().entrySet()) {
            config.getEventPublisher().registerAggregateEventPayloadTransformer(entry.getKey(), entry.getValue());
        }
    }

    private void registerMessageListeners() {
        for (var listener : createMessageListeners()) {
            config.getMessaging().registerAggregateEventMessageListener(listener);
        }
    }

}
