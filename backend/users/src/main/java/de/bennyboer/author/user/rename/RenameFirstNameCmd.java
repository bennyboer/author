package de.bennyboer.author.user.rename;

import de.bennyboer.author.eventsourcing.command.Command;
import de.bennyboer.author.user.FirstName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RenameFirstNameCmd implements Command {

    FirstName firstName;

    public static RenameFirstNameCmd of(FirstName firstName) {
        checkNotNull(firstName, "First name must be given");

        return new RenameFirstNameCmd(firstName);
    }

}
