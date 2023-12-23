package de.bennyboer.author.project.create;

import de.bennyboer.author.project.ProjectName;
import de.bennyboer.author.eventsourcing.command.Command;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateCmd implements Command {

    ProjectName name;

    public static CreateCmd of(ProjectName name) {
        checkNotNull(name, "Name must not be null");

        return new CreateCmd(name);
    }

}
