package de.bennyboer.author.user;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkArgument;
import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserName {

    String value;

    public static UserName of(String value) {
        checkNotNull(value, "User name must be given");
        checkArgument(!value.contains("@"), "User name must not contain '@'");

        return new UserName(value);
    }

    @Override
    public String toString() {
        return String.format("UserName(%s)", value);
    }

}
