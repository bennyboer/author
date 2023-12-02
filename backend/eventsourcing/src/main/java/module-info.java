module de.bennyboer.eventsourcing {
    requires static lombok;
    requires reactor.core;
    requires org.reactivestreams;
    exports de.bennyboer.eventsourcing.api;
    exports de.bennyboer.eventsourcing.api.event.metadata;
    exports de.bennyboer.eventsourcing.api.event;
    exports de.bennyboer.eventsourcing.api.event.metadata.agent;
    exports de.bennyboer.eventsourcing;
    exports de.bennyboer.eventsourcing.api.aggregate;
    exports de.bennyboer.eventsourcing.api.command;
    exports de.bennyboer.eventsourcing.api.persistence;
    exports de.bennyboer.eventsourcing.api.patch;
}