package de.bennyboer.author.server.projects.transformer;

import de.bennyboer.author.project.Project;
import de.bennyboer.author.server.projects.api.ProjectDTO;
import de.bennyboer.author.server.projects.persistence.lookup.LookupProject;

public class ProjectTransformer {

    public static ProjectDTO toApi(Project project) {
        return ProjectDTO.builder()
                .id(project.getId().getValue())
                .version(project.getVersion().getValue())
                .name(project.getName().getValue())
                .createdAt(project.getCreatedAt())
                .build();
    }

    public static ProjectDTO toApi(LookupProject project) {
        return ProjectDTO.builder()
                .id(project.getId().getValue())
                .version(project.getVersion().getValue())
                .name(project.getName().getValue())
                .createdAt(project.getCreatedAt())
                .build();
    }

}
