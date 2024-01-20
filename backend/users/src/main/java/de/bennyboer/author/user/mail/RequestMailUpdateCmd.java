package de.bennyboer.author.user.mail;

import de.bennyboer.author.eventsourcing.command.Command;
import de.bennyboer.author.user.Mail;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestMailUpdateCmd implements Command {

    Mail mail;

    public static RequestMailUpdateCmd of(Mail mail) {
        checkNotNull(mail, "Mail must be given");

        return new RequestMailUpdateCmd(mail);
    }

}
