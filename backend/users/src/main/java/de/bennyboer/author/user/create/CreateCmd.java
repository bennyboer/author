package de.bennyboer.author.user.create;

import de.bennyboer.author.user.Password;
import de.bennyboer.author.user.UserName;
import de.bennyboer.author.eventsourcing.command.Command;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateCmd implements Command {

    UserName name;

    Password password;

    public static CreateCmd of(UserName name, Password password) {
        checkNotNull(name, "Name must be given");
        checkNotNull(password, "Password must be given");

        return new CreateCmd(name, password);
    }

}
