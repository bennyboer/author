package de.bennyboer.author.project.rename;

import de.bennyboer.author.project.ProjectName;
import de.bennyboer.author.eventsourcing.command.Command;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RenameCmd implements Command {

    ProjectName newName;

    public static RenameCmd of(ProjectName newName) {
        checkNotNull(newName, "New name must be given");

        return new RenameCmd(newName);
    }

}
