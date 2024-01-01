package de.bennyboer.author.server.structure.persistence.lookup;

import de.bennyboer.author.structure.StructureId;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LookupStructure {

    StructureId id;

    String projectId;

    public static LookupStructure of(StructureId id, String projectId) {
        checkNotNull(id, "Structure ID must be given");
        checkNotNull(projectId, "Project ID must be given");

        return new LookupStructure(id, projectId);
    }

}
