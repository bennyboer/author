package de.bennyboer.author.server.projects.persistence.lookup;

public class SQLiteProjectLookupRepoTests extends ProjectLookupRepoTests {

    @Override
    protected ProjectLookupRepo createRepo() {
        return new SQLiteProjectLookupRepo(true);
    }

}
