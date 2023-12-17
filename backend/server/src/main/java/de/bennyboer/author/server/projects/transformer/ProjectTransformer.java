package de.bennyboer.author.server.projects.transformer;

import de.bennyboer.author.project.Project;
import de.bennyboer.author.server.projects.api.ProjectDTO;

public class ProjectTransformer {

    public static ProjectDTO toApi(Project project) {
        return ProjectDTO.builder()
                .id(project.getId().getValue())
                .version(project.getVersion().getValue())
                .name(project.getName().getValue())
                .createdAt(project.getCreatedAt())
                .build();
    }

}
