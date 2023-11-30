package de.bennyboer.author.server;

import io.javalin.Javalin;
import lombok.Value;

public class App {

    public static void main(String[] args) {
        @Value
        class Test {

            String msg;
        }

        Javalin.create()
                .get("/", ctx -> ctx.result("Hello World"))
                .ws("/ws", ws -> {
                    ws.onConnect(session -> {
                        System.out.println("Connected");
                        session.send(new Test("Hello from server"));
                    });
                })
                .start(7070);
    }

}
