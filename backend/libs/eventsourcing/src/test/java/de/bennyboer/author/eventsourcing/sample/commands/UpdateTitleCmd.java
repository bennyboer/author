package de.bennyboer.author.eventsourcing.sample.commands;

import de.bennyboer.author.eventsourcing.command.Command;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkArgument;
import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateTitleCmd implements Command {

    String title;

    public static UpdateTitleCmd of(String title) {
        checkNotNull(title, "title must be given");
        checkArgument(!title.isBlank(), "title must not be blank");

        return new UpdateTitleCmd(title);
    }

}
