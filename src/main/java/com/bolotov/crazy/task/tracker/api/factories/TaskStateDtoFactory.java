package com.bolotov.crazy.task.tracker.api.factories;

import com.bolotov.crazy.task.tracker.api.dto.TaskDto;
import com.bolotov.crazy.task.tracker.api.dto.TaskStateDto;
import com.bolotov.crazy.task.tracker.store.entities.TaskEntity;
import com.bolotov.crazy.task.tracker.store.entities.TaskStateEntity;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Component
public class TaskStateDtoFactory {

    TaskDtoFactory taskDtoFactory;

    public TaskStateDto makeTaskStateDto(TaskStateEntity taskStateEntity) {
        return TaskStateDto.builder()
                .id(taskStateEntity.getId())
                .name(taskStateEntity.getName())
                .createdAt(taskStateEntity.getCreatedAt())
                .leftTaskStateId(taskStateEntity
                        .getLeftTaskState()
                        .map(TaskStateEntity::getId)
                        .orElse(null))
                .rightTaskStateId(taskStateEntity
                        .getRightTaskState()
                        .map(TaskStateEntity::getId)
                        .orElse(null))
                .tasks(taskStateEntity
                        .getTasks()
                        .stream()
                        .map(taskDtoFactory::makeTaskDto)
                        .toList())
                .build();
    }
}
