package de.bennyboer.author.permissions;

import de.bennyboer.author.permissions.repo.PermissionsRepo;
import de.bennyboer.author.permissions.repo.SQLitePermissionsRepo;

public class SQLiteRepoPermissionsServiceTests extends PermissionsServiceTests {

    @Override
    protected PermissionsRepo createRepo() {
        return new SQLitePermissionsRepo("test", true);
    }

}
