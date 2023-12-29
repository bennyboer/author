package de.bennyboer.author.user.login;

import de.bennyboer.author.user.Password;
import de.bennyboer.author.eventsourcing.command.Command;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LoginCmd implements Command {

    Password password;

    Instant now;

    public static LoginCmd of(Password password, Instant now) {
        checkNotNull(password, "Password must be given");
        checkNotNull(now, "Now must be given");

        return new LoginCmd(password, now);
    }

}
