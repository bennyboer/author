package de.bennyboer.author.project;

import de.bennyboer.author.eventsourcing.EventPublisher;
import de.bennyboer.author.eventsourcing.EventSourcingService;
import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.aggregate.AggregateId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateIdAndVersion;
import de.bennyboer.author.eventsourcing.aggregate.AggregateService;
import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.author.eventsourcing.persistence.EventSourcingRepo;
import de.bennyboer.author.project.create.CreateCmd;
import de.bennyboer.author.project.remove.RemoveCmd;
import de.bennyboer.author.project.rename.RenameCmd;
import reactor.core.publisher.Mono;

import java.util.List;

public class ProjectsService extends AggregateService<Project, ProjectId> {

    public ProjectsService(EventSourcingRepo repo, EventPublisher eventPublisher) {
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
