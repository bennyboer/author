package de.bennyboer.author.eventsourcing.sample.commands;

import de.bennyboer.author.eventsourcing.command.Command;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateDescriptionCmd implements Command {

    String description;

    public static UpdateDescriptionCmd of(String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description must not be null or empty");
        }

        return new UpdateDescriptionCmd(description);
    }

}
