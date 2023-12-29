package de.bennyboer.author.user.rename;

import de.bennyboer.author.user.UserName;
import de.bennyboer.author.eventsourcing.command.Command;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RenameCmd implements Command {

    UserName newName;

    public static RenameCmd of(UserName newName) {
        checkNotNull(newName, "New name must be given");

        return new RenameCmd(newName);
    }

}
