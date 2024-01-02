package de.bennyboer.author.persistence.patches;

import de.bennyboer.author.persistence.RepositoryVersion;

import java.sql.Connection;

public abstract class JDBCRepositoryPatch implements RepositoryPatch {

    private final RepositoryVersion appliesTo;

    protected JDBCRepositoryPatch(RepositoryVersion appliesTo) {
        this.appliesTo = appliesTo;
    }

    @Override
    public RepositoryVersion appliesTo() {
        return appliesTo;
    }

    /**
     * Apply the patch in a transaction.
     * All changes will be rolled back if an exception is thrown.
     */
    public abstract void apply(Connection connection) throws Exception;

}
