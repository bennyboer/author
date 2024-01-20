package de.bennyboer.author.user.password;

import de.bennyboer.author.eventsourcing.command.Command;
import de.bennyboer.author.user.Password;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChangePasswordCmd implements Command {

    Password password;

    public static ChangePasswordCmd of(Password password) {
        checkNotNull(password, "Password must be given");

        return new ChangePasswordCmd(password);
    }

}
