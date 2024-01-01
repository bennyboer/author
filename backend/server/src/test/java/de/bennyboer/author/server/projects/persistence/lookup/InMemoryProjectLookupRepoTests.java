package de.bennyboer.author.server.projects.persistence.lookup;

public class InMemoryProjectLookupRepoTests extends ProjectLookupRepoTests {

    @Override
    protected ProjectLookupRepo createRepo() {
        return new InMemoryProjectLookupRepo();
    }
    
}
