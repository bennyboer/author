module de.bennyboer.author.server {
    requires static lombok;
    requires jakarta.annotation;
    requires io.javalin;
    requires reactor.core;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jdk8;
    requires de.bennyboer.author.structure;
    requires de.bennyboer.eventsourcing;
    requires de.bennyboer.author.common;
    exports de.bennyboer.author.server.websocket.messages;
    opens de.bennyboer.author.server.websocket.messages;
    exports de.bennyboer.author.server.structure.api;
    opens de.bennyboer.author.server.structure.api;
    exports de.bennyboer.author.server.structure.api.requests;
    opens de.bennyboer.author.server.structure.api.requests;
}