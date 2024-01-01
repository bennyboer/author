package de.bennyboer.author.server.users.persistence.lookup;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.user.UserName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class UserLookupRepoTests {

    private final UserLookupRepo repo = createRepo();

    protected abstract UserLookupRepo createRepo();

    @Test
    void shouldInsertUser() {
        UserId userId = UserId.of("USER_ID");
        UserName userName = UserName.of("USER_NAME");

        // given: a user to insert
        var user = LookupUser.of(userId, userName);

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
        var user = LookupUser.of(userId, userName);
        repo.update(user).block();

        // when: updating the user
        UserName newUserName = UserName.of("NEW_USER_NAME");
        var updatedUser = LookupUser.of(userId, newUserName);
        repo.update(updatedUser).block();

        // then: the user can be found by its new name
        var actualUserId = repo.findUserIdByName(newUserName).block();
        assertThat(actualUserId).isEqualTo(userId);

        // and: the user can no longer be found by its old name
        var oldUserId = repo.findUserIdByName(userName).block();
        assertThat(oldUserId).isNull();
    }

    @Test
    void shouldRemoveUser() {
        UserId userId = UserId.of("USER_ID");
        UserName userName = UserName.of("USER_NAME");

        // given: a user
        var user = LookupUser.of(userId, userName);
        repo.update(user).block();

        // when: removing the user
        repo.remove(userId).block();

        // then: the user can no longer be found by its name
        var actualUserId = repo.findUserIdByName(userName).block();
        assertThat(actualUserId).isNull();
    }

    @Test
    void shouldCountUsers() {
        // given: some users
        var user1 = LookupUser.of(UserId.of("USER_ID_1"), UserName.of("USER_NAME_1"));
        var user2 = LookupUser.of(UserId.of("USER_ID_2"), UserName.of("USER_NAME_2"));
        var user3 = LookupUser.of(UserId.of("USER_ID_3"), UserName.of("USER_NAME_3"));

        repo.update(user1).block();
        repo.update(user2).block();
        repo.update(user3).block();

        // when: counting users
        var count = repo.countUsers().block();

        // then: the count is 3
        assertThat(count).isEqualTo(3);
    }

}
