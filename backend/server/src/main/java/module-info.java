module de.bennyboer.author.server {
    requires static lombok;
    requires jakarta.annotation;
    requires io.javalin;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jdk8;
    requires de.bennyboer.author.structure;
    exports de.bennyboer.author.server.websocket.messages;
    opens de.bennyboer.author.server.websocket.messages;
}