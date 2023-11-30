package de.bennyboer.eventsourcing.sample.commands;

import de.bennyboer.eventsourcing.api.command.Command;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateCmd implements Command {

    String title;

    String description;

    public static CreateCmd of(String title, String description) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title must not be null or empty");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description must not be null or empty");
        }

        return new CreateCmd(title, description);
    }

}
