package de.bennyboer.author.structure.tree;

import de.bennyboer.eventsourcing.api.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TreeIdAndVersion {

    TreeId id;

    Version version;

    public static TreeIdAndVersion of(TreeId id, Version version) {
        if (id == null) {
            throw new IllegalArgumentException("Id must not be null");
        }
        if (version == null) {
            throw new IllegalArgumentException("Version must not be null");
        }

        return new TreeIdAndVersion(id, version);
    }

}
