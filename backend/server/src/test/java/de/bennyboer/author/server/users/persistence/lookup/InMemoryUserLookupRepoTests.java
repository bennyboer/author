package de.bennyboer.author.server.users.persistence.lookup;

public class InMemoryUserLookupRepoTests extends UserLookupRepoTests {

    @Override
    protected UserLookupRepo createRepo() {
        return new InMemoryUserLookupRepo();
    }

}
