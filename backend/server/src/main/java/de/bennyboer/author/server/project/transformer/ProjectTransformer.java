package de.bennyboer.author.server.project.transformer;

import de.bennyboer.author.project.Project;
import de.bennyboer.author.server.project.api.ProjectDTO;

public class ProjectTransformer {

    public static ProjectDTO toApi(Project project) {
        return ProjectDTO.builder()
                .id(project.getId().getValue())
                .version(project.getVersion())
                .name(project.getName().getValue())
                .createdAt(project.getCreatedAt())
                .build();
    }

}
