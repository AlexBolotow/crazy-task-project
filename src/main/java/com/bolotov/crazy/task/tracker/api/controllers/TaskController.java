package com.bolotov.crazy.task.tracker.api.controllers;

import com.bolotov.crazy.task.tracker.api.controllers.helpers.ControllerHelper;
import com.bolotov.crazy.task.tracker.api.dto.TaskDto;
import com.bolotov.crazy.task.tracker.api.dto.TaskStateDto;
import com.bolotov.crazy.task.tracker.api.exceptions.BadRequestException;
import com.bolotov.crazy.task.tracker.api.factories.TaskDtoFactory;
import com.bolotov.crazy.task.tracker.store.entities.ProjectEntity;
import com.bolotov.crazy.task.tracker.store.entities.TaskEntity;
import com.bolotov.crazy.task.tracker.store.entities.TaskStateEntity;
import com.bolotov.crazy.task.tracker.store.repositories.TaskRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
@RestController
@Tag(name = "task")
public class TaskController {

    TaskRepository taskRepository;

    TaskDtoFactory taskDtoFactory;

    ControllerHelper controllerHelper;

    public static final String GET_TASKS = "/api/projects/{project_id}/task-states/{task_state_id}/tasks";
    public static final String CREATE_TASK = "/api/projects/{project_id}/task-states/{task_state_id}/tasks";

    @GetMapping(GET_TASKS)
    @Operation(summary = "Получение задач")
    public List<TaskDto> getTasks(
            @PathVariable(name = "project_id") Long projectId,
            @PathVariable(name = "task_state_id") Long taskStateId) {

        ProjectEntity projectEntity = controllerHelper.getProjectOrThrowException(projectId);

        TaskStateEntity taskStateEntity = controllerHelper.getTaskStateOrThrowException(taskStateId);

        return taskStateEntity
                .getTasks()
                .stream()
                .map(taskDtoFactory::makeTaskDto)
                .toList();
    }

    @PostMapping(CREATE_TASK)
    @Operation(summary = "Создание задачи")
    public TaskDto createTask(
            @PathVariable(name = "project_id") Long projectId,
            @PathVariable(name = "task_state_id") Long taskStateId,
            @RequestParam(value = "task_name") String taskName) {

        if (taskName.trim().isEmpty()) {
            throw new BadRequestException("Task name can`t be empty.");
        }

        ProjectEntity projectEntity = controllerHelper.getProjectOrThrowException(projectId);

        TaskStateEntity taskStateEntity = controllerHelper.getTaskStateOrThrowException(taskStateId);

        taskStateEntity
                .getTasks()
                .stream()
                .map(TaskEntity::getName)
                .filter(anotherTaskStateName -> anotherTaskStateName.equalsIgnoreCase(taskName))
                .findAny()
                .ifPresent(anotherTaskStateName -> {

                    throw new BadRequestException(String.format("Task \"%s\" already exists.", taskName));

                });

        final TaskEntity savedTask = taskRepository.saveAndFlush(
                TaskEntity.builder()
                        .name(taskName)
                        .task_state(taskStateEntity)
                        .build()
        );

        return taskDtoFactory.makeTaskDto(savedTask);
    }
}
