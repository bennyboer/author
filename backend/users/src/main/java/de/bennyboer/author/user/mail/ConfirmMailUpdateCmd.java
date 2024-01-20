package de.bennyboer.author.user.mail;

import de.bennyboer.author.eventsourcing.command.Command;
import de.bennyboer.author.user.Mail;
import de.bennyboer.author.user.MailConfirmationToken;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ConfirmMailUpdateCmd implements Command {

    Mail mail;

    MailConfirmationToken token;

    public static ConfirmMailUpdateCmd of(Mail mail, MailConfirmationToken token) {
        checkNotNull(mail, "Mail must be given");
        checkNotNull(token, "Mail confirmation token must be given");

        return new ConfirmMailUpdateCmd(mail, token);
    }

}
