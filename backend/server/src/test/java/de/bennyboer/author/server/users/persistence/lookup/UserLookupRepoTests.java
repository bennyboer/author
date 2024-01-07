package de.bennyboer.author.server.users.persistence.lookup;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.user.Mail;
import de.bennyboer.author.user.UserName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;

public abstract class UserLookupRepoTests {

    private final UserLookupRepo repo = createRepo();

    protected abstract UserLookupRepo createRepo();

    @Test
    void shouldInsertUser() {
        UserId userId = UserId.of("USER_ID");
        UserName userName = UserName.of("USER_NAME");
        Mail mail = Mail.of("default+test@example.com");

        // given: a user to insert
        var user = LookupUser.of(userId, userName, mail);

        // when: inserting the user
        repo.update(user).block();

        // then: the user can be found by its name
        var actualUserId = repo.findUserIdByName(userName).block();
        assertThat(actualUserId).isEqualTo(userId);
    }

    @Test
    void shouldUpdateUser() {
        // given: a user
        UserId userId = UserId.of("USER_ID");
        UserName userName = UserName.of("USER_NAME");
        Mail mail = Mail.of("default+test@example.com");
        var user = LookupUser.of(userId, userName, mail);
        repo.update(user).block();

        // when: updating the user
        UserName newUserName = UserName.of("NEW_USER_NAME");
        Mail newMail = Mail.of("new-mail+test@example.com");
        var updatedUser = LookupUser.of(userId, newUserName, newMail);
        repo.update(updatedUser).block();

        // then: the user can be found by its new name
        var actualUserId = repo.findUserIdByName(newUserName).block();
        assertThat(actualUserId).isEqualTo(userId);

        // and: the user can be found by its new mail
        var actualUserIdByMail = repo.findUserIdByMail(newMail).block();
        assertThat(actualUserIdByMail).isEqualTo(userId);

        // and: the user can no longer be found by its old name
        var oldUserId = repo.findUserIdByName(userName).block();
        assertThat(oldUserId).isNull();

        // and: the user can no longer be found by its old mail
        var oldUserIdByMail = repo.findUserIdByMail(mail).block();
        assertThat(oldUserIdByMail).isNull();
    }

    @Test
    void shouldRemoveUser() {
        UserId userId = UserId.of("USER_ID");
        UserName userName = UserName.of("USER_NAME");
        Mail mail = Mail.of("default+test@example.com");

        // given: a user
        var user = LookupUser.of(userId, userName, mail);
        repo.update(user).block();

        // when: removing the user
        repo.remove(userId).block();

        // then: the user can no longer be found by its name
        var actualUserId = repo.findUserIdByName(userName).block();
        assertThat(actualUserId).isNull();

        // and: the user can no longer be found by its mail
        var actualUserIdByMail = repo.findUserIdByMail(mail).block();
        assertThat(actualUserIdByMail).isNull();
    }

    @Test
    void shouldCountUsers() {
        // given: some users
        var user1 = LookupUser.of(UserId.of("USER_ID_1"), UserName.of("USER_NAME_1"), Mail.of("1+test@example.com"));
        var user2 = LookupUser.of(UserId.of("USER_ID_2"), UserName.of("USER_NAME_2"), Mail.of("2+test@example.com"));
        var user3 = LookupUser.of(UserId.of("USER_ID_3"), UserName.of("USER_NAME_3"), Mail.of("3+test@example.com"));

        repo.update(user1).block();
        repo.update(user2).block();
        repo.update(user3).block();

        // when: counting users
        var count = repo.countUsers().block();

        // then: the count is 3
        assertThat(count).isEqualTo(3);
    }

    @Test
    void shouldNotAllowTwoUsersWithTheSameUserName() {
        // given: a user
        var user1 = LookupUser.of(
                UserId.of("USER_ID_1"),
                UserName.of("USER_NAME"),
                Mail.of("default+test@example.com")
        );
        repo.update(user1).block();

        // when: inserting another user with the same name
        var user2 = LookupUser.of(
                UserId.of("USER_ID_2"),
                UserName.of("USER_NAME"),
                Mail.of("hello+test@example.com")
        );

        // then: an exception is thrown
        assertThatException().isThrownBy(() -> repo.update(user2).block());
    }

    @Test
    void shouldNotAllowTwoUsersWithTheSameMail() {
        // given: a user
        var user1 = LookupUser.of(
                UserId.of("USER_ID_1"),
                UserName.of("USER_NAME_1"),
                Mail.of("hello+test@example.com")
        );
        repo.update(user1).block();

        // when: inserting another user with the same mail
        var user2 = LookupUser.of(
                UserId.of("USER_ID_2"),
                UserName.of("USER_NAME_2"),
                Mail.of("hello+test@example.com")
        );

        // then: an exception is thrown
        assertThatException().isThrownBy(() -> repo.update(user2).block());
    }

}
