package com.bolotov.crazy.task.tracker.api.controllers;

import com.bolotov.crazy.task.tracker.api.dto.AckDto;
import com.bolotov.crazy.task.tracker.api.dto.ProjectDto;
import com.bolotov.crazy.task.tracker.api.exceptions.BadRequestException;
import com.bolotov.crazy.task.tracker.api.exceptions.NotFoundException;
import com.bolotov.crazy.task.tracker.api.factories.ProjectDtoFactory;
import com.bolotov.crazy.task.tracker.store.entities.ProjectEntity;
import com.bolotov.crazy.task.tracker.store.repositories.ProjectRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
@RestController
public class ProjectController {
    ProjectRepository projectRepository;

    ProjectDtoFactory projectDtoFactory;

    public static final String FETCH_PROJECTS = "/api/projects";
    public static final String CREATE_PROJECT = "/api/projects";
    public static final String EDIT_PROJECT = "/api/projects/{project_id}";
    public static final String DELETE_PROJECT = "/api/projects/{project_id}";

    public static final String CREATE_OR_UPDATE_PROJECT = "/api/projects";

    @GetMapping(FETCH_PROJECTS)
    public List<ProjectDto> fetchProjects(
            @RequestParam(value = "prefix_name", required = false) Optional<String> optionalPrefixName) {

        optionalPrefixName = optionalPrefixName
                .filter(prefixName -> !prefixName.trim().isEmpty());

        Stream<ProjectEntity> projectEntityStream;
        if (optionalPrefixName.isPresent()) {
            projectEntityStream = projectRepository
                    .streamAllByNameStartsWithIgnoreCase(optionalPrefixName.get());
        } else {
            projectEntityStream = projectRepository.streamAllBy();
        }

        return projectEntityStream
                .map(projectDtoFactory::makeProjectDto)
                .toList();
    }

    @PostMapping(CREATE_PROJECT)
    public ProjectDto createProject(@RequestParam("project_name") String projectName) {
        if (projectName.trim().isEmpty()) {
            throw new BadRequestException("Name can`t be empty.");
        }

        projectRepository
                .findByName(projectName)
                .ifPresent(p -> {
                    throw new BadRequestException(String.format("Project \"%s\" already exists.", projectName));
                });

        ProjectEntity projectEntity = projectRepository.saveAndFlush(
                ProjectEntity.builder()
                        .name(projectName)
                        .build()
        );

        return projectDtoFactory.makeProjectDto(projectEntity);
    }

    @PutMapping(CREATE_OR_UPDATE_PROJECT)
    public ProjectDto createOrUpdateProject(
            @RequestParam(value = "project_id", required = false) Optional<Long> optionalProjectId,
            @RequestParam(value = "project_name", required = false) Optional<String> optionalProjectName) {

        optionalProjectName = optionalProjectName.filter(projectName -> projectName.trim().isEmpty());

        boolean isCreate = optionalProjectId.isEmpty();

        ProjectEntity projectEntity = optionalProjectId.
                map(this::getProjectOrThrowException)
                .orElseGet(() -> ProjectEntity.builder().build());

        if (isCreate && optionalProjectName.isEmpty()) {
            throw new BadRequestException("Name can`t be empty.");
        }

        optionalProjectName
                .ifPresent(projectName -> {
                    projectRepository
                            .findByName(projectName)
                            .filter(anotherProject -> !Objects.equals(anotherProject.getId(), projectEntity.getId()))
                            .ifPresent(anotherProject -> {
                                throw new BadRequestException(String.format("Project \"%s\" already exists.", projectName));
                            });

                    projectEntity.setName(projectName);
                });

        final ProjectEntity savedProject = projectRepository.saveAndFlush(projectEntity);

        return projectDtoFactory.makeProjectDto(savedProject);
    }


    @PatchMapping(EDIT_PROJECT)
    public ProjectDto editProject(
            @PathVariable("project_id") Long projectId,
            @RequestParam String projectName) {

        if (projectName.trim().isEmpty()) {
            throw new BadRequestException("Name can`t be empty.");
        }

        ProjectEntity projectEntity = projectRepository
                .findById(projectId)
                .orElseThrow(() ->
                        new NotFoundException(
                                String.format(
                                        "Project with \"%s\" doesn`t exists.",
                                        projectId
                                )
                        )
                );

        //Если проект с таким именем уже существует
        projectRepository
                .findByName(projectName)
                .filter(anotherProject -> !Objects.equals(anotherProject.getId(), projectId))
                .ifPresent(anotherProject -> {
                    throw new BadRequestException(String.format("Project \"%s\" already exists.", projectName));
                });

        projectEntity.setName(projectName);

        projectEntity = projectRepository.saveAndFlush(projectEntity);

        return projectDtoFactory.makeProjectDto(projectEntity);
    }

    @DeleteMapping(DELETE_PROJECT)
    public AckDto deleteProject(@PathVariable("project_id") Long projectId) {

        projectRepository
                .findById(projectId)
                .orElseThrow(() ->
                        new NotFoundException(
                                String.format(
                                        "Project with \"%s\" doesn`t exists.",
                                        projectId
                                )
                        )
                );

        projectRepository.deleteById(projectId);

        return AckDto.makeDefault(true);
    }

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
