package de.bennyboer.author.server.shared.websocket;

import de.bennyboer.author.auth.token.Token;
import de.bennyboer.author.common.UserId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.author.server.shared.http.Auth;
import de.bennyboer.author.server.shared.messaging.Messaging;
import de.bennyboer.author.server.shared.messaging.events.AggregateEventMessage;
import de.bennyboer.author.server.shared.messaging.permissions.AggregatePermissionEventMessage;
import de.bennyboer.author.server.shared.websocket.api.*;
import de.bennyboer.author.server.shared.websocket.subscriptions.SubscriptionManager;
import de.bennyboer.author.server.shared.websocket.subscriptions.events.AggregateEventPermissionChecker;
import de.bennyboer.author.server.shared.websocket.subscriptions.events.AggregateEventSubscriptionManager;
import de.bennyboer.author.server.shared.websocket.subscriptions.events.EventSubscriptionTarget;
import de.bennyboer.author.server.shared.websocket.subscriptions.permissions.AggregatePermissionEventSubscriptionManager;
import de.bennyboer.author.server.shared.websocket.subscriptions.permissions.PermissionEventSubscriptionTarget;
import io.javalin.websocket.*;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class WebSocketService {

    private final SubscriptionManager<EventSubscriptionTarget> aggregateEventSubscriptionManager;
    private final SubscriptionManager<PermissionEventSubscriptionTarget> aggregatePermissionEventSubscriptionManager;

    private final Map<AggregateType, AggregateEventPermissionChecker> eventPermissionCheckers = new HashMap<>();

    private final Map<SessionId, WsContext> sessions = new ConcurrentHashMap<>();

    public WebSocketService(Messaging messaging) {
        aggregateEventSubscriptionManager = new AggregateEventSubscriptionManager(
                messaging,
                this::publishAggregateEvent
        );
        aggregatePermissionEventSubscriptionManager = new AggregatePermissionEventSubscriptionManager(
                messaging,
                this::publishAggregatePermissionEvent
        );
    }

    public void onConnect(WsConnectContext ctx) {
        SessionId sessionId = SessionId.of(ctx);
        sessions.put(sessionId, ctx);
        log.debug("User connected via WebSocket with session ID '{}'", sessionId.getValue());
    }

    public void onClose(WsCloseContext ctx) {
        SessionId sessionId = SessionId.of(ctx);
        closeSessionIfOpen(sessionId);
        aggregateEventSubscriptionManager.unsubscribeFromAllTargets(sessionId);

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

    public void registerSubscriptionPermissionChecker(AggregateEventPermissionChecker permissionChecker) {
        eventPermissionCheckers.put(permissionChecker.getAggregateType(), permissionChecker);
    }

    private void publishAggregateEvent(
            AggregateEventMessage msg,
            Set<SessionId> subscribers
    ) {
        String aggregateType = msg.getAggregateType();
        String aggregateId = msg.getAggregateId();
        long aggregateVersion = msg.getAggregateVersion();
        long eventVersion = msg.getEventVersion();
        String eventName = msg.getEventName();
        Map<String, Object> payload = msg.getPayload();

        var topic = EventTopicDTO.builder()
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .version(aggregateVersion)
                .build();

        EventMessage eventMessage = EventMessage.of(
                topic,
                eventName,
                eventVersion,
                payload
        );

        publishMessageToSubscribers(WebSocketMessage.event(eventMessage), subscribers);
    }

    private void publishAggregatePermissionEvent(
            AggregatePermissionEventMessage msg,
            Set<SessionId> subscribers
    ) {
        String aggregateType = msg.getAggregateType();
        String aggregateId = msg.getAggregateId().orElse(null);
        String action = msg.getAction();
        String userId = msg.getUserId();

        var type = switch (msg.getEventType()) {
            case ADDED -> PermissionEventMessage.EventType.ADDED;
            case REMOVED -> PermissionEventMessage.EventType.REMOVED;
        };

        PermissionEventMessage permissionEvent = PermissionEventMessage.of(
                type,
                userId,
                aggregateType,
                aggregateId,
                action
        );

        publishMessageToSubscribers(WebSocketMessage.permissionEvent(permissionEvent), subscribers);
    }

    private void publishMessageToSubscribers(WebSocketMessage msg, Set<SessionId> subscribers) {
        for (var subscriber : findSubscribedSessions(subscribers)) {
            subscriber.send(msg);
        }
    }

    private Iterable<WsContext> findSubscribedSessions(Set<SessionId> sessionIds) {
        return sessionIds.stream()
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
            case SUBSCRIBE_TO_PERMISSIONS -> subscribeToPermissions(
                    ctx,
                    msg.getSubscribeToPermissions().orElseThrow(),
                    agent
            );
            case UNSUBSCRIBE -> unsubscribe(ctx, msg.getUnsubscribe().orElseThrow());
            case UNSUBSCRIBE_FROM_PERMISSIONS -> unsubscribeFromPermissions(
                    ctx,
                    msg.getUnsubscribeFromPermissions().orElseThrow(),
                    agent
            );
            default -> throw new IllegalArgumentException(
                    "Encountered message with unsupported method from client" + msg.getMethod()
            );
        }
    }

    private void subscribe(WsContext ctx, SubscribeMessage msg, Agent agent) {
        EventSubscriptionTarget target = EventSubscriptionTarget.of(
                msg.getAggregateType(),
                msg.getAggregateId(),
                msg.getEventName().orElse(null)
        );
        assertThatAgentIsAllowedToSubscribeToTargetEvents(target, agent);

        aggregateEventSubscriptionManager.subscribe(target, SessionId.of(ctx));
    }

    private void subscribeToPermissions(
            WsContext ctx,
            SubscribeToPermissionsMessage msg,
            Agent agent
    ) {
        PermissionEventSubscriptionTarget target = PermissionEventSubscriptionTarget.of(
                msg.getAggregateType(),
                msg.getAggregateId().orElse(null),
                agent.getUserId().orElse(null)
        );

        aggregatePermissionEventSubscriptionManager.subscribe(target, SessionId.of(ctx));
    }

    private void unsubscribe(WsContext ctx, UnsubscribeMessage msg) {
        EventSubscriptionTarget target = EventSubscriptionTarget.of(
                msg.getAggregateType(),
                msg.getAggregateId(),
                msg.getEventName().orElse(null)
        );

        aggregateEventSubscriptionManager.unsubscribe(target, SessionId.of(ctx));
    }

    private void unsubscribeFromPermissions(
            WsContext ctx,
            UnsubscribeFromPermissionsMessage msg,
            Agent agent
    ) {
        UserId userId = agent.getUserId().orElse(null);
        PermissionEventSubscriptionTarget target = PermissionEventSubscriptionTarget.of(
                msg.getAggregateType(),
                msg.getAggregateId().orElse(null),
                userId
        );

        aggregatePermissionEventSubscriptionManager.unsubscribe(target, SessionId.of(ctx));
    }

    private void closeSessionIfOpen(SessionId sessionId) {
        Optional.ofNullable(sessions.remove(sessionId))
                .filter(ctx -> ctx.session.isOpen())
                .ifPresent(ctx -> ctx.session.close());
    }

    private void assertThatAgentIsAllowedToSubscribeToTargetEvents(EventSubscriptionTarget target, Agent agent) {
        AggregateEventPermissionChecker permissionChecker = eventPermissionCheckers.get(target.getAggregateType());

        if (permissionChecker == null) {
            throw new IllegalArgumentException(
                    "No permission checker registered for aggregate type " + target.getAggregateType()
            );
        }

        var hasPermission = permissionChecker.hasPermissionToReceiveEvents(
                agent,
                target.getAggregateId()
        ).block();
        if (!hasPermission) {
            throw new IllegalArgumentException(
                    agent + " is not allowed to subscribe to events of aggregate " + target.getAggregateId()
            );
        }
    }

}
