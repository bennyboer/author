package de.bennyboer.author.user;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.apache.commons.validator.routines.EmailValidator;

import static de.bennyboer.author.common.Preconditions.checkArgument;
import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Mail {

    String value;

    public static Mail of(String value) {
        checkNotNull(value, "Mail value must be given");
        checkArgument(
                EmailValidator.getInstance().isValid(value),
                String.format("Mail value '%s' is not valid", value)
        );

        return new Mail(value);
    }

    @Override
    public String toString() {
        return String.format("Mail(%s)", value);
    }

}
