package de.bennyboer.author.project.commands;

import de.bennyboer.author.project.model.ProjectName;
import de.bennyboer.eventsourcing.api.command.Command;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateProjectCmd implements Command {

    ProjectName name;

    public static CreateProjectCmd of(ProjectName name) {
        checkNotNull(name, "Name must not be null");

        return new CreateProjectCmd(name);
    }

}
