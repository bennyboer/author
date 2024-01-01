package de.bennyboer.author.server.structure.persistence.lookup;

import de.bennyboer.author.structure.StructureId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class StructureLookupRepoTests {

    private final StructureLookupRepo repo = createRepo();

    protected abstract StructureLookupRepo createRepo();

    @Test
    void shouldInsertStructure() {
        StructureId id = StructureId.of("STRUCTURE_ID");

        // given: a structure to insert
        var structure = LookupStructure.of(id, "PROJECT_ID");

        // when: the structure is inserted
        repo.update(structure).block();

        // then: the structure ID can be found by project ID
        var actualId = repo.findStructureIdByProjectId("PROJECT_ID").block();
        assertThat(actualId).isEqualTo(id);
    }

    @Test
    void shouldUpdateStructure() {
        // given: a structure
        var structure = LookupStructure.of(StructureId.of("STRUCTURE_ID"), "PROJECT_ID");
        repo.update(structure).block();

        // when: the structure is updated
        var updatedStructure = LookupStructure.of(StructureId.of("STRUCTURE_ID"), "PROJECT_ID_2");
        repo.update(updatedStructure).block();

        // then: the structure ID can be found by the new project ID
        var actualId = repo.findStructureIdByProjectId("PROJECT_ID_2").block();
        assertThat(actualId).isEqualTo(StructureId.of("STRUCTURE_ID"));

        // and: the structure ID cannot be found by the old project ID
        var oldId = repo.findStructureIdByProjectId("PROJECT_ID").block();
        assertThat(oldId).isNull();
    }

    @Test
    void shouldRemoveStructure() {
        // given: a structure to remove
        var structure = LookupStructure.of(StructureId.of("STRUCTURE_ID"), "PROJECT_ID");
        repo.update(structure).block();

        // when: the structure is removed
        repo.remove(StructureId.of("STRUCTURE_ID")).block();

        // then: the structure ID cannot be found by the project ID
        var actualId = repo.findStructureIdByProjectId("PROJECT_ID").block();
        assertThat(actualId).isNull();
    }

}
