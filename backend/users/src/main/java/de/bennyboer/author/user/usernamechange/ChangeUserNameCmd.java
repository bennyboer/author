package de.bennyboer.author.user.usernamechange;

import de.bennyboer.author.eventsourcing.command.Command;
import de.bennyboer.author.user.UserName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChangeUserNameCmd implements Command {

    UserName newName;

    public static ChangeUserNameCmd of(UserName newName) {
        checkNotNull(newName, "New name must be given");

        return new ChangeUserNameCmd(newName);
    }

}
