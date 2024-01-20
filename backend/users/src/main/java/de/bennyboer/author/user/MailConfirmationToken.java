package de.bennyboer.author.user;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.UUID;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MailConfirmationToken {

    String value;

    public static MailConfirmationToken of(String value) {
        checkNotNull(value, "Mail confirmation token value must be given");

        return new MailConfirmationToken(value);
    }

    public static MailConfirmationToken create() {
        return of(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return String.format("MailConfirmationToken(%s)", value);
    }

}
