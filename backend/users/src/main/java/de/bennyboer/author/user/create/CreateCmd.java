package de.bennyboer.author.user.create;

import de.bennyboer.author.user.Password;
import de.bennyboer.author.user.UserName;
import de.bennyboer.eventsourcing.command.Command;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateCmd implements Command {

    UserName name;

    Password password;

    public static CreateCmd of(UserName name, Password password) {
        checkNotNull(name, "Name must not be null");
        checkNotNull(password, "Password must not be null");

        return new CreateCmd(name, password);
    }

}
