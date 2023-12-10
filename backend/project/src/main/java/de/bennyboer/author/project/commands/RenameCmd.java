package de.bennyboer.author.project.commands;

import de.bennyboer.author.project.ProjectName;
import de.bennyboer.eventsourcing.command.Command;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RenameCmd implements Command {

    ProjectName newName;

    public static RenameCmd of(ProjectName newName) {
        checkNotNull(newName, "New name must not be null");

        return new RenameCmd(newName);
    }

}
