package de.bennyboer.author.user.error;

public class UserLockedException extends RuntimeException {

    public UserLockedException(String message) {
        super(message);
    }

}
