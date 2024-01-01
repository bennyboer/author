package de.bennyboer.author.server.structure.persistence.lookup;

public class SQLiteStructureLookupRepoTests extends StructureLookupRepoTests {

    @Override
    protected StructureLookupRepo createRepo() {
        return new SQLiteStructureLookupRepo(true);
    }

}
