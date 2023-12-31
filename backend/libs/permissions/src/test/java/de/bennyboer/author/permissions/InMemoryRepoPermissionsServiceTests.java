package de.bennyboer.author.permissions;

import de.bennyboer.author.permissions.repo.InMemoryPermissionsRepo;
import de.bennyboer.author.permissions.repo.PermissionsRepo;

public class InMemoryRepoPermissionsServiceTests extends PermissionsServiceTests {

    @Override
    protected PermissionsRepo createRepo() {
        return new InMemoryPermissionsRepo();
    }

}
