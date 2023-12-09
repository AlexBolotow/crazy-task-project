package com.bolotov.crazy.task.tracker.api.controllers.helpers;

import com.bolotov.crazy.task.tracker.api.exceptions.NotFoundException;
import com.bolotov.crazy.task.tracker.store.entities.ProjectEntity;
import com.bolotov.crazy.task.tracker.store.repositories.ProjectRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Component
@Transactional
public class ControllerHelper {

    ProjectRepository projectRepository;

    public ProjectEntity getProjectOrThrowException(Long projectId) {

        return projectRepository
                .findById(projectId)
                .orElseThrow(() ->
                        new NotFoundException(
                                String.format(
                                        "Project with \"%s\" doesn`t exists.",
                                        projectId
                                )
                        )
                );
    }
}
