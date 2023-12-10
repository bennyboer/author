package de.bennyboer.author.user.commands;

import de.bennyboer.author.user.UserName;
import de.bennyboer.eventsourcing.command.Command;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RenameCmd implements Command {

    UserName newName;

    public static RenameCmd of(UserName newName) {
        checkNotNull(newName, "New name must not be null");

        return new RenameCmd(newName);
    }

}
