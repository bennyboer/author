package de.bennyboer.author.structure.tree.events;

import de.bennyboer.author.structure.tree.commands.CreateCmd;
import de.bennyboer.author.structure.tree.node.Node;
import de.bennyboer.eventsourcing.api.Version;
import de.bennyboer.eventsourcing.api.event.Event;
import de.bennyboer.eventsourcing.api.event.EventName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CreatedEvent implements Event {

    public static final EventName NAME = EventName.of("CREATED");

    public static final Version VERSION = Version.zero();

    Node rootNode;

    public static CreatedEvent of(CreateCmd cmd) {
        return new CreatedEvent(cmd.getRootNode());
    }

    @Override
    public EventName getName() {
        return NAME;
    }

    @Override
    public Version getVersion() {
        return VERSION;
    }

}
