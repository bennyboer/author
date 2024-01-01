package de.bennyboer.author.server.structure.persistence.lookup;

public class InMemoryStructureLookupRepoTests extends StructureLookupRepoTests {

    @Override
    protected StructureLookupRepo createRepo() {
        return new InMemoryStructureLookupRepo();
    }

}
