package de.bennyboer.author.server.users.persistence.lookup;

public class SQLiteUserLookupRepoTests extends UserLookupRepoTests {

    @Override
    protected UserLookupRepo createRepo() {
        return new SQLiteUserLookupRepo(true);
    }

}
