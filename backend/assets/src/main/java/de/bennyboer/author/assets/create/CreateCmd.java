package de.bennyboer.author.assets.create;

import de.bennyboer.author.assets.Content;
import de.bennyboer.author.eventsourcing.command.Command;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateCmd implements Command {

    Content content;

    public static CreateCmd of(Content content) {
        checkNotNull(content, "Content must be given");

        return new CreateCmd(content);
    }

}
