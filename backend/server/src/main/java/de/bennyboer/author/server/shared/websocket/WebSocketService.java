package de.bennyboer.author.server.shared.websocket;

import de.bennyboer.author.auth.token.Token;
import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.eventsourcing.event.EventName;
import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.author.server.shared.http.Auth;
import de.bennyboer.author.server.shared.messaging.Messaging;
import de.bennyboer.author.server.shared.websocket.api.*;
import de.bennyboer.author.server.shared.websocket.subscriptions.EventPermissionChecker;
import de.bennyboer.author.server.shared.websocket.subscriptions.EventTopic;
import de.bennyboer.author.server.shared.websocket.subscriptions.SubscriptionManager;
import de.bennyboer.author.server.shared.websocket.subscriptions.SubscriptionTarget;
import io.javalin.websocket.*;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class WebSocketService {

    private final SubscriptionManager subscriptionManager;

    private final Map<AggregateType, EventPermissionChecker> eventPermissionCheckers = new HashMap<>();

    private final Map<SessionId, WsContext> sessions = new ConcurrentHashMap<>();

    public WebSocketService(Messaging messaging) {
        subscriptionManager = new SubscriptionManager(messaging, this::publishEvent);
    }

    public void onConnect(WsConnectContext ctx) {
        SessionId sessionId = SessionId.of(ctx);
        sessions.put(sessionId, ctx);
        log.debug("User connected via WebSocket with session ID '{}'", sessionId.getValue());
    }

    public void onClose(WsCloseContext ctx) {
        SessionId sessionId = SessionId.of(ctx);
        closeSessionIfOpen(sessionId);
        subscriptionManager.unsubscribeFromAllTargets(sessionId);

        log.debug(
                "Closed WebSocket for session ID '{}' with status code '{}' and reason '{}'",
                sessionId.getValue(),
                ctx.status(),
                ctx.reason()
        );
    }

    public void onError(WsErrorContext ctx) {
        SessionId sessionId = SessionId.of(ctx);
        closeSessionIfOpen(sessionId);

        log.error(
                "Error in WebSocket for session ID '{}'",
                sessionId.getValue(),
                ctx.error()
        );
    }

    public void onMessage(WsMessageContext ctx) {
        SessionId sessionId = SessionId.of(ctx);

        log.debug(
                "Received message '{}' from session ID '{}'",
                ctx.message(),
                sessionId.getValue()
        );

        WebSocketMessage msg = ctx.messageAsClass(WebSocketMessage.class);
        onMessage(ctx, msg);
    }

    public void publishEvent(
            EventTopic eventTopic,
            EventName eventName,
            Version eventVersion,
            Map<String, Object> payload
    ) {
        var topic = EventTopicDTO.builder()
                .aggregateType(eventTopic.getAggregateType().getValue())
                .aggregateId(eventTopic.getAggregateId().getValue())
                .version(eventTopic.getVersion().getValue())
                .build();

        var msg = WebSocketMessage.event(EventMessage.of(
                topic,
                eventName.getValue(),
                eventVersion.getValue(),
                payload
        ));

        publishEventMsgToSubscribers(eventTopic, msg);
    }

    public void registerSubscriptionPermissionChecker(EventPermissionChecker permissionChecker) {
        eventPermissionCheckers.put(permissionChecker.getAggregateType(), permissionChecker);
    }

    private void publishEventMsgToSubscribers(EventTopic topic, WebSocketMessage msg) {
        for (var subscriber : findSubscribers(topic)) {
            subscriber.send(msg);
        }
    }

    private Iterable<WsContext> findSubscribers(EventTopic topic) {
        SubscriptionTarget target = topic.toSubscriptionTarget();

        return subscriptionManager.getSubscribers(target)
                .stream()
                .flatMap(sessionId -> Optional.ofNullable(sessions.get(sessionId)).stream())
                .toList();
    }

    private void onMessage(WsContext ctx, WebSocketMessage msg) {
        Token token = msg.getToken().map(Token::of).orElseThrow(() -> new IllegalArgumentException(
                "Received websocket message without token"
        ));
        Agent agent = Auth.toAgent(token).block();

        if (agent.isAnonymous()) {
            log.warn("Received message from anonymous user - closing websocket session");
            ctx.session.close(4001, "Unauthorized");
            return;
        }

        switch (msg.getMethod()) {
            case HEARTBEAT -> ctx.send(WebSocketMessage.heartbeat());
            case SUBSCRIBE -> subscribe(
                    ctx,
                    msg.getSubscribe().orElseThrow(),
                    agent
            );
            case UNSUBSCRIBE -> unsubscribe(ctx, msg.getUnsubscribe().orElseThrow());
            default -> throw new IllegalArgumentException(
                    "Encountered message with unsupported method from client" + msg.getMethod()
            );
        }
    }

    private void subscribe(WsContext ctx, SubscribeMessage msg, Agent agent) {
        assertThatAgentIsAllowedToSubscribeToTargetEvents(msg.getTarget(), agent);

        subscriptionManager.subscribe(msg.getTarget(), SessionId.of(ctx));
    }

    private void unsubscribe(WsContext ctx, UnsubscribeMessage msg) {
        subscriptionManager.unsubscribe(msg.getTarget(), SessionId.of(ctx));
    }

    private void closeSessionIfOpen(SessionId sessionId) {
        Optional.ofNullable(sessions.remove(sessionId))
                .filter(ctx -> ctx.session.isOpen())
                .ifPresent(ctx -> ctx.session.close());
    }

    private void assertThatAgentIsAllowedToSubscribeToTargetEvents(SubscriptionTarget target, Agent agent) {
        EventPermissionChecker permissionChecker = eventPermissionCheckers.get(target.getAggregateType());

        if (permissionChecker == null) {
            throw new IllegalArgumentException(
                    "No permission checker registered for aggregate type " + target.getAggregateType()
            );
        }

        var hasPermission = permissionChecker.hasPermissionToReceiveEvents(agent, target.getAggregateId()).block();
        if (!hasPermission) {
            throw new IllegalArgumentException(
                    agent + " is not allowed to subscribe to events of aggregate " + target.getAggregateId()
            );
        }
    }

}
