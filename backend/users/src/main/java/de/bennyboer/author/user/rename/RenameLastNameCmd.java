package de.bennyboer.author.user.rename;

import de.bennyboer.author.eventsourcing.command.Command;
import de.bennyboer.author.user.LastName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RenameLastNameCmd implements Command {

    LastName lastName;

    public static RenameLastNameCmd of(LastName lastName) {
        checkNotNull(lastName, "Last name must be given");
        
        return new RenameLastNameCmd(lastName);
    }

}
