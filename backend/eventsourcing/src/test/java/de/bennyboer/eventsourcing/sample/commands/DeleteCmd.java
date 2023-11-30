package de.bennyboer.eventsourcing.sample.commands;

import de.bennyboer.eventsourcing.api.command.Command;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DeleteCmd implements Command {

    public static DeleteCmd of() {
        return new DeleteCmd();
    }

}
