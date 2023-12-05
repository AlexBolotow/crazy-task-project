package com.bolotov.crazy.task.tracker.api.factories;

import com.bolotov.crazy.task.tracker.api.dto.ProjectDto;
import com.bolotov.crazy.task.tracker.store.entities.ProjectEntity;
import org.springframework.stereotype.Component;

@Component
public class ProjectDtoFactory {
    public ProjectDto makeProjectDto(ProjectEntity projectEntity) {
        return ProjectDto.builder()
                .id(projectEntity.getId())
                .name(projectEntity.getName())
                .createdAt(projectEntity.getCreatedAt())
                .build();
    }
}
