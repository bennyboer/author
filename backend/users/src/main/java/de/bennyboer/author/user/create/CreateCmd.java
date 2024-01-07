package de.bennyboer.author.user.create;

import de.bennyboer.author.eventsourcing.command.Command;
import de.bennyboer.author.user.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateCmd implements Command {

    UserName name;

    Mail mail;

    FirstName firstName;

    LastName lastName;

    Password password;

    public static CreateCmd of(UserName name, Mail mail, FirstName firstName, LastName lastName, Password password) {
        checkNotNull(name, "Name must be given");
        checkNotNull(mail, "Mail must be given");
        checkNotNull(firstName, "First name must be given");
        checkNotNull(lastName, "Last name must be given");
        checkNotNull(password, "Password must be given");

        return new CreateCmd(name, mail, firstName, lastName, password);
    }

}
