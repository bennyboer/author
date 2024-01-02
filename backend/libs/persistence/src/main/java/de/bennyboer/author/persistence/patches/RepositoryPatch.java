package de.bennyboer.author.persistence.patches;

import de.bennyboer.author.persistence.RepositoryVersion;

/**
 * A patch that can be applied to the repository/database.
 * Patches are applied in order, so you can rely on the fact that all previous patches have been applied.
 * If a patch is applied, the version of the repository will be increased by one.
 * <p>
 * If you want the server to run without downtime, you need to make sure to only add fields to the database,
 * never remove them, except you are sure that old server-instances can run without them.
 * If you want to remove a field you'll mostly do the following:
 * - Add the new fields using a patch
 * - Deploy the new version of the server that will apply the patch
 * - Wait for all old server-instances to be shut down
 * - Remove the old fields using another patch
 */
public interface RepositoryPatch {

    /**
     * The version this patch applies to.
     */
    RepositoryVersion appliesTo();

}
