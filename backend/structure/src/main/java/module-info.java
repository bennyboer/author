module de.bennyboer.author.structure {
    requires static lombok;
    requires de.bennyboer.eventsourcing;
    requires de.bennyboer.author.common;
    requires jakarta.annotation;
    requires reactor.core;
    exports de.bennyboer.author.structure.tree.api;
}