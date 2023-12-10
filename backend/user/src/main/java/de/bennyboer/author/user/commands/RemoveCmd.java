package de.bennyboer.author.user.commands;

import de.bennyboer.eventsourcing.command.Command;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RemoveCmd implements Command {

    public static RemoveCmd of() {
        return new RemoveCmd();
    }

}
