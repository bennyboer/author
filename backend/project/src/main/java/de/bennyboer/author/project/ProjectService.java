package de.bennyboer.author.project;

import de.bennyboer.author.project.commands.CreateCmd;
import de.bennyboer.author.project.commands.RemoveCmd;
import de.bennyboer.author.project.commands.RenameCmd;
import de.bennyboer.eventsourcing.EventPublisher;
import de.bennyboer.eventsourcing.EventSourcingService;
import de.bennyboer.eventsourcing.Version;
import de.bennyboer.eventsourcing.aggregate.AggregateId;
import de.bennyboer.eventsourcing.aggregate.AggregateIdAndVersion;
import de.bennyboer.eventsourcing.aggregate.AggregateService;
import de.bennyboer.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.eventsourcing.persistence.EventSourcingRepo;
import reactor.core.publisher.Mono;

import java.util.List;

public class ProjectService extends AggregateService<Project, ProjectId> {

    public ProjectService(EventSourcingRepo repo, EventPublisher eventPublisher) {
        super(new EventSourcingService<>(
                Project.TYPE,
                Project.init(),
                repo,
                eventPublisher,
                List.of()
        ));
    }

    public Mono<AggregateIdAndVersion<ProjectId>> create(ProjectName name, Agent agent) {
        ProjectId id = ProjectId.create();

        return dispatchCommandToLatest(id, agent, CreateCmd.of(name))
                .map(version -> AggregateIdAndVersion.of(id, version));
    }

    public Mono<Version> rename(ProjectId id, Version version, ProjectName newName, Agent agent) {
        return dispatchCommand(id, version, agent, RenameCmd.of(newName));
    }

    public Mono<Version> remove(ProjectId id, Version version, Agent agent) {
        return dispatchCommand(id, version, agent, RemoveCmd.of());
    }

    @Override
    protected AggregateId toAggregateId(ProjectId projectId) {
        return AggregateId.of(projectId.getValue());
    }

    @Override
    protected boolean isRemoved(Project aggregate) {
        return aggregate.isRemoved();
    }

}
