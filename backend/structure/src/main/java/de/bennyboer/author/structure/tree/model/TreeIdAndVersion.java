package de.bennyboer.author.structure.tree.model;

import de.bennyboer.eventsourcing.api.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TreeIdAndVersion {

    TreeId id;

    Version version;

    public static TreeIdAndVersion of(TreeId id, Version version) {
        checkNotNull(id, "Id must not be null");
        checkNotNull(version, "Version must not be null");

        return new TreeIdAndVersion(id, version);
    }

}
