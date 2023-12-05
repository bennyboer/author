package de.bennyboer.eventsourcing.sample.commands;

import de.bennyboer.eventsourcing.api.command.Command;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.common.Preconditions.checkArgument;
import static de.bennyboer.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateTitleCmd implements Command {

    String title;

    public static UpdateTitleCmd of(String title) {
        checkNotNull(title, "title must not be null");
        checkArgument(!title.isBlank(), "title must not be blank");

        return new UpdateTitleCmd(title);
    }

}
