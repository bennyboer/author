package de.bennyboer.eventsourcing.sample.commands;

import de.bennyboer.eventsourcing.api.command.Command;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateTitleCmd implements Command {

    String title;

    public static UpdateTitleCmd of(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title must not be null or empty");
        }

        return new UpdateTitleCmd(title);
    }

}
