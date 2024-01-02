package de.bennyboer.author.persistence.sqlite.patches;

import de.bennyboer.author.persistence.RepositoryVersion;
import de.bennyboer.author.persistence.patches.JDBCRepositoryPatch;
import de.bennyboer.author.persistence.sqlite.SQLiteRepository;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SQLiteRepoWithPatchesTests {

    private final Path tempPath;

    {
        try {
            tempPath = Files.createTempFile("test", ".sqlite3");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldReadVersion0() throws Exception {
        // given: a previously closed repository
        try (var repository = new TestRepo(List.of())) {
            // when: reading the version
            var version = repository.getVersion().block();

            // then: the version should be 0
            assertThat(version).isEqualTo(RepositoryVersion.zero());
        }

        // when: reopening the repository
        try (var repository = new TestRepo(List.of())) {
            // then: the version should be 0
            var version = repository.getVersion().block();

            // then: the version should be 0
            assertThat(version).isEqualTo(RepositoryVersion.zero());
        }
    }

    @Test
    void shouldUpdateVersionTo1() throws Exception {
        // given: a closed repository
        try (var repository = new TestRepo(List.of())) {
            // when: getting the version
            var version = repository.getVersion().block();

            // then: the version should be 0
            assertThat(version).isEqualTo(RepositoryVersion.zero());
        }

        // when: reopening the repository with a patch
        try (
                var repository = new TestRepo(List.of(
                        new NoopPatch(RepositoryVersion.zero())
                ))
        ) {
            // then: the version should be 1
            var version = repository.getVersion().block();

            // then: the version should be 1
            assertThat(version).isEqualTo(RepositoryVersion.of(1));
        }
    }

    @Test
    void shouldUpdateVersionTo3WithOutOfOrderPatches() throws Exception {
        // given: a closed repository
        try (var repository = new TestRepo(List.of())) {
            // when: getting the version
            var version = repository.getVersion().block();

            // then: the version should be 0
            assertThat(version).isEqualTo(RepositoryVersion.zero());
        }

        // when: reopening the repository with 3 patches given out of order
        try (
                var repository = new TestRepo(List.of(
                        new NoopPatch(RepositoryVersion.of(2)),
                        new NoopPatch(RepositoryVersion.zero()),
                        new NoopPatch(RepositoryVersion.of(1))
                ))
        ) {
            // then: the version should be 3
            var version = repository.getVersion().block();

            // then: the version should be 3
            assertThat(version).isEqualTo(RepositoryVersion.of(3));
        }
    }

    @Test
    void shouldRollbackWhenExceptionIsThrownDuringPatch() throws Exception {
        // given: a closed repository
        try (var repository = new TestRepo(List.of())) {
            // when: getting the version
            var version = repository.getVersion().block();

            // then: the version should be 0
            assertThat(version).isEqualTo(RepositoryVersion.zero());
        }

        // when: reopening the repository with a patch that throws an exception
        try (
                var repository = new TestRepo(List.of(
                        new JDBCRepositoryPatch(RepositoryVersion.zero()) {
                            @Override
                            public void apply(Connection connection) throws Exception {
                                // First do a successful SQL operation that should be rolled back
                                try (var statement = connection.createStatement()) {
                                    statement.execute("CREATE TABLE test (id INTEGER PRIMARY KEY)");
                                }

                                // Then throw an exception
                                throw new Exception("Test exception");
                            }
                        }
                ))
        ) {
            // then: the version should be 0
            var version = repository.getVersion().block();
            assertThat(version).isEqualTo(RepositoryVersion.zero());

            // and: the table should not exist
            assertThat(repository.tableExists("test")).isFalse();
        }
    }

    @Test
    void shouldRollbackAnotherPatchAfterApplingAPatchSuccessfully() throws Exception {
        // given: a closed repository
        try (var repository = new TestRepo(List.of())) {
            // when: getting the version
            var version = repository.getVersion().block();

            // then: the version should be 0
            assertThat(version).isEqualTo(RepositoryVersion.zero());
        }

        // when: reopening the repository with two patches (one of which throws an exception)
        try (
                var repository = new TestRepo(List.of(
                        new JDBCRepositoryPatch(RepositoryVersion.zero()) {
                            @Override
                            public void apply(Connection connection) throws Exception {
                                try (var statement = connection.createStatement()) {
                                    statement.execute("CREATE TABLE test (id INTEGER PRIMARY KEY)");
                                }
                            }
                        },
                        new JDBCRepositoryPatch(RepositoryVersion.of(1)) {
                            @Override
                            public void apply(Connection connection) throws Exception {
                                // Then throw an exception
                                throw new Exception("Test exception");
                            }
                        }
                ))
        ) {
            // then: the version should be 1
            var version = repository.getVersion().block();
            assertThat(version).isEqualTo(RepositoryVersion.of(1));

            // and: the table should exist
            assertThat(repository.tableExists("test")).isTrue();
        }
    }

    private class TestRepo extends SQLiteRepository {

        public TestRepo(List<JDBCRepositoryPatch> patches) {
            super("test", patches, true);
        }

        public boolean tableExists(String tableName) {
            try {
                return tableExists(getConnection(), tableName);
            } catch (SQLException | IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected Path getFilePath() {
            return tempPath;
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
