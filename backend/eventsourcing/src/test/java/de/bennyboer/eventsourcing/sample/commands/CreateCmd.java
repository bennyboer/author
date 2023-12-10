package de.bennyboer.eventsourcing.sample.commands;

import de.bennyboer.eventsourcing.command.Command;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateCmd implements Command {

    String title;

    String description;

    /**
     * Note that it does not really make sense to include this field in the command and created event.
     * It is just here to demonstrate a patch.
     */
    Instant deletedAt;

    public static CreateCmd of(String title, String description, Instant deletedAt) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title must not be null or empty");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description must not be null or empty");
        }

        return new CreateCmd(title, description, deletedAt);
    }

}
