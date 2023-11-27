package de.bennyboer.author.server;

import io.javalin.Javalin;

public class App {

    public static void main(String[] args) {
        Javalin.create()
                .get("/", ctx -> ctx.result("Hello World"))
                .start(7070);
    }

}
