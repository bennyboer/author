package de.bennyboer.author.server.users.persistence.lookup;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.user.Mail;
import de.bennyboer.author.user.UserName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LookupUser {

    UserId id;

    UserName name;

    Mail mail;

    public static LookupUser of(UserId id, UserName name, Mail mail) {
        checkNotNull(id, "User ID must be given");
        checkNotNull(name, "User name must be given");
        checkNotNull(mail, "User mail must be given");

        return new LookupUser(id, name, mail);
    }

}
