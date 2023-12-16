package de.bennyboer.author.user.login;

public class UserLockedException extends RuntimeException {

    public UserLockedException(String message) {
        super(message);
    }

}
