package com.bolotov.crazy.task.tracker.api.controllers;

import com.bolotov.crazy.task.tracker.api.controllers.helpers.ControllerHelper;
import com.bolotov.crazy.task.tracker.api.dto.AckDto;
import com.bolotov.crazy.task.tracker.api.dto.TaskStateDto;
import com.bolotov.crazy.task.tracker.api.exceptions.BadRequestException;
import com.bolotov.crazy.task.tracker.api.exceptions.NotFoundException;
import com.bolotov.crazy.task.tracker.api.factories.TaskStateDtoFactory;
import com.bolotov.crazy.task.tracker.store.entities.ProjectEntity;
import com.bolotov.crazy.task.tracker.store.entities.TaskStateEntity;
import com.bolotov.crazy.task.tracker.store.repositories.TaskStateRepository;
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
@Tag(name = "task state")
public class TaskStateController {

    TaskStateRepository taskStateRepository;

    TaskStateDtoFactory taskStateDtoFactory;

    ControllerHelper controllerHelper;

    public static final String GET_TASK_STATES = "/api/projects/{project_id}/task-states";
    public static final String CREATE_TASK_STATE = "/api/projects/{project_id}/task-states";
    public static final String UPDATE_TASK_STATE = "/api/task-states/{task_state_id}";
    public static final String CHANGE_TASK_STATES_POSITION = "/api/task-states/{task_state_id}/position/change";
    public static final String DELETE_TASK_STATE = "/api/task-states/{task_state_id}";

    @GetMapping(GET_TASK_STATES)
    @Operation(summary = "Получение статусов задач")
    public List<TaskStateDto> getTaskStates(@PathVariable(name = "project_id") Long projectId) {

        ProjectEntity projectEntity = controllerHelper.getProjectOrThrowException(projectId);

        return projectEntity
                .getTaskStates()
                .stream()
                .map(taskStateDtoFactory::makeTaskStateDto)
                .toList();
    }

    @PostMapping(CREATE_TASK_STATE)
    @Operation(summary = "Создание статуса задач")
    public TaskStateDto createTaskState(
            @PathVariable(name = "project_id") Long projectId,
            @RequestParam(value = "task_state_name") String taskStateName) {

        if (taskStateName.trim().isEmpty()) {
            throw new BadRequestException("Task state name can`t be empty.");
        }

        ProjectEntity projectEntity = controllerHelper.getProjectOrThrowException(projectId);

        projectEntity
                .getTaskStates()
                .stream()
                .map(TaskStateEntity::getName)
                .filter(anotherTaskStateName -> anotherTaskStateName.equalsIgnoreCase(taskStateName))
                .findAny()
                .ifPresent(anotherTaskStateName -> {

                    throw new BadRequestException(String.format("Task state \"%s\" already exists.", taskStateName));

                });

        Optional<TaskStateEntity> optionalTaskStateEntity = taskStateRepository
                .findTaskStateEntityByRightTaskStateIsNullAndProjectId(projectId);

        TaskStateEntity taskStateEntity = taskStateRepository.saveAndFlush(
                TaskStateEntity.builder()
                        .name(taskStateName)
                        .project(projectEntity)
                        .build()
        );

        optionalTaskStateEntity
                .ifPresent(anotherTaskState -> {

                    taskStateEntity.setLeftTaskState(anotherTaskState);

                    anotherTaskState.setRightTaskState(taskStateEntity);

                    taskStateRepository.saveAndFlush(anotherTaskState);
                });

        final TaskStateEntity savedTaskState = taskStateRepository.saveAndFlush(taskStateEntity);

        return taskStateDtoFactory.makeTaskStateDto(savedTaskState);
    }

    @PatchMapping(UPDATE_TASK_STATE)
    @Operation(summary = "Изменение статуса задач")
    public TaskStateDto updateTaskState(
            @PathVariable(name = "task_state_id") Long taskStateId,
            @RequestParam(name = "task_state_name") String taskStateName) {

        if (taskStateName.trim().isEmpty()) {
            throw new BadRequestException("Task state name can`t be empty.");
        }

        TaskStateEntity taskStateEntity = getTaskStateOrThrowException(taskStateId);

        //нет ли task state c таким же именем в этом проекте
        taskStateRepository
                .findTaskStateByProjectIdAndNameContainsIgnoreCase(
                        taskStateEntity.getProject().getId(),
                        taskStateName)
                .filter(anotherTaskState -> !anotherTaskState.getId().equals(taskStateId))
                .ifPresent(anotherTaskState -> {
                    throw new BadRequestException(String.format("Task state \"%s\" already exists.", taskStateName));
                });

        taskStateEntity.setName(taskStateName);

        taskStateEntity = taskStateRepository.saveAndFlush(taskStateEntity);

        return taskStateDtoFactory.makeTaskStateDto(taskStateEntity);
    }


    @PatchMapping(CHANGE_TASK_STATES_POSITION)
    @Operation(summary = "Изменение позиции статуса задач")
    public TaskStateDto changeTaskStatePosition(
            @PathVariable(name = "task_state_id") Long taskStateId,
            @RequestParam(name = "left_task_state_id", required = false) Optional<Long> optionalLeftTaskStateId) {

        TaskStateEntity changeTaskState = getTaskStateOrThrowException(taskStateId);

        ProjectEntity project = changeTaskState.getProject();

        Optional<Long> optionalOldLeftTaskStateId = changeTaskState
                .getLeftTaskState()
                .map(TaskStateEntity::getId);

        if (optionalOldLeftTaskStateId.equals(optionalLeftTaskStateId)) {
            return taskStateDtoFactory.makeTaskStateDto(changeTaskState);
        }

        Optional<TaskStateEntity> optionalNewLeftTaskState = optionalLeftTaskStateId
                .map(leftTaskStateId -> {

                    if (taskStateId.equals(leftTaskStateId)) {
                        throw new BadRequestException("Left task state id equals changed task state.");
                    }

                    TaskStateEntity leftTaskStateEntity = getTaskStateOrThrowException(leftTaskStateId);

                    if (!project.getId().equals(leftTaskStateEntity.getProject().getId())) {
                        throw new BadRequestException("Task state position can be changed within the same project.");
                    }

                    return leftTaskStateEntity;
                });

        Optional<TaskStateEntity> optionalNewRightTaskState;
        if (!optionalNewLeftTaskState.isPresent()) {

            optionalNewRightTaskState = project
                    .getTaskStates()
                    .stream()
                    .filter(anotherTaskState -> !anotherTaskState.getLeftTaskState().isPresent())
                    .findAny();
        } else {

            optionalNewRightTaskState = optionalNewLeftTaskState
                    .get()
                    .getRightTaskState();
        }

        replaceOldTaskStatePosition(changeTaskState);

        if (optionalNewLeftTaskState.isPresent()) {

            TaskStateEntity newLeftTaskState = optionalNewLeftTaskState.get();

            newLeftTaskState.setRightTaskState(changeTaskState);

            changeTaskState.setLeftTaskState(newLeftTaskState);
        } else {
            changeTaskState.setLeftTaskState(null);
        }

        if (optionalNewRightTaskState.isPresent()) {

            TaskStateEntity newRightTaskState = optionalNewRightTaskState.get();

            newRightTaskState.setLeftTaskState(changeTaskState);

            changeTaskState.setRightTaskState(newRightTaskState);
        } else {
            changeTaskState.setRightTaskState(null);
        }

        changeTaskState = taskStateRepository.saveAndFlush(changeTaskState);

        optionalNewLeftTaskState
                .ifPresent(taskStateRepository::saveAndFlush);

        optionalNewRightTaskState
                .ifPresent(taskStateRepository::saveAndFlush);

        return taskStateDtoFactory.makeTaskStateDto(changeTaskState);
    }

    @DeleteMapping(DELETE_TASK_STATE)
    @Operation(summary = "Удаление статуса задач")
    public AckDto deleteTaskState(@PathVariable(name = "task_state_id") Long taskStateId) {

        TaskStateEntity changeTaskState = getTaskStateOrThrowException(taskStateId);

        replaceOldTaskStatePosition(changeTaskState);

        taskStateRepository.delete(changeTaskState);

        return AckDto.builder().answer(true).build();
    }

    private void replaceOldTaskStatePosition(TaskStateEntity changeTaskState) {

        Optional<TaskStateEntity> optionalOldLeftTaskState = changeTaskState.getLeftTaskState();
        Optional<TaskStateEntity> optionalOldRightTaskState = changeTaskState.getRightTaskState();

        optionalOldLeftTaskState
                .ifPresent(it -> {

                    it.setRightTaskState(optionalOldRightTaskState.orElse(null));

                    taskStateRepository.saveAndFlush(it);
                });

        optionalOldRightTaskState
                .ifPresent(it -> {

                    it.setLeftTaskState(optionalOldLeftTaskState.orElse(null));

                    taskStateRepository.saveAndFlush(it);
                });
    }

    private TaskStateEntity getTaskStateOrThrowException(Long taskStateId) {

        return taskStateRepository
                .findById(taskStateId)
                .orElseThrow(() ->
                        new NotFoundException(
                                String.format(
                                        "Task state with \"%s\" doesn`t exists.", taskStateId)
                        )
                );
    }
}
