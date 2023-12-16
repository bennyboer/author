package de.bennyboer.author.user;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserName {

    String value;

    public static UserName of(String value) {
        checkNotNull(value, "User name must not be null");

        return new UserName(value);
    }

    @Override
    public String toString() {
        return String.format("UserName(%s)", value);
    }
    
}
