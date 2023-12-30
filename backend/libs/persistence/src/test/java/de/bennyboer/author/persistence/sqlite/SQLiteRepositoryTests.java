package de.bennyboer.author.persistence.sqlite;

import de.bennyboer.author.persistence.RepositoryVersion;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SQLiteRepositoryTests {

    @Test
    void shouldBeAbleToReadVersion() throws Exception {
        // given: a repository
        try (var repository = new SQLiteRepository("test", true)) {
            // when: getting the version
            RepositoryVersion version = repository.getVersion().block();

            // then: the version should be 0
            assertThat(version).isEqualTo(RepositoryVersion.of(0));
        }
    }

}
