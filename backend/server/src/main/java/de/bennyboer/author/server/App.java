package de.bennyboer.author.server;

import de.bennyboer.author.server.websocket.WebSocketService;
import io.javalin.Javalin;
import lombok.Value;

public class App {

    public static void main(String[] args) {
        var webSocketService = WebSocketService.getInstance();

        Javalin.create()
                .get("/", ctx -> ctx.result("Hello World"))
                .ws("/ws", ws -> {
                    ws.onConnect(webSocketService::onConnect);
                    ws.onClose(webSocketService::onClose);
                    ws.onError(webSocketService::onError);
                    ws.onMessage(webSocketService::onMessage);
                })
                .start(7070);
    }

}
