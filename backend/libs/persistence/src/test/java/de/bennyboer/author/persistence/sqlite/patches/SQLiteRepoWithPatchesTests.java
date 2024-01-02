package de.bennyboer.author.persistence.sqlite.patches;

import de.bennyboer.author.persistence.RepositoryVersion;
import de.bennyboer.author.persistence.patches.JDBCRepositoryPatch;
import de.bennyboer.author.persistence.patches.PatchManager;
import de.bennyboer.author.persistence.sqlite.SQLiteRepository;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SQLiteRepoWithPatchesTests {

    @Test
    void shouldReadVersion0() throws Exception {
        // given: a repository without patch
        try (var repository = new TestRepo(List.of())) {
            // when: reading the version
            var version = repository.getVersion().block();

            // then: the version should be 0
            assertThat(version).isEqualTo(RepositoryVersion.zero());
        }
    }

    @Test
    void shouldUpdateVersionTo1() throws Exception {
        // given: a repository with a patch
        try (
                var repository = new TestRepo(List.of(
                        new NoopPatch(RepositoryVersion.zero())
                ))
        ) {
            // when: getting the version
            var version = repository.getVersion().block();

            // then: the version should be 1
            assertThat(version).isEqualTo(RepositoryVersion.of(1));
        }
    }

    @Test
    void shouldUpdateVersionTo3() throws Exception {
        // given: a repository with 3 patches
        try (
                var repository = new TestRepo(List.of(
                        new NoopPatch(RepositoryVersion.of(2)),
                        new NoopPatch(RepositoryVersion.zero()),
                        new NoopPatch(RepositoryVersion.of(1))
                ))
        ) {
            // when: getting the version
            var version = repository.getVersion().block();

            // then: the version should be 3
            assertThat(version).isEqualTo(RepositoryVersion.of(3));
        }
    }

    private static class TestRepo extends SQLiteRepository {

        public TestRepo(List<JDBCRepositoryPatch> patches) {
            super("test", new PatchManager<>(patches), true);
        }

    }

    private static class NoopPatch extends JDBCRepositoryPatch {

        public NoopPatch(RepositoryVersion version) {
            super(version);
        }

        @Override
        public void apply(Connection connection) {
            // Do nothing
        }

    }

}
