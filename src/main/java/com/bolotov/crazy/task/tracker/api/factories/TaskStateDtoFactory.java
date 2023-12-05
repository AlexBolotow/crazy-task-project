package com.bolotov.crazy.task.tracker.api.factories;

import com.bolotov.crazy.task.tracker.api.dto.TaskDto;
import com.bolotov.crazy.task.tracker.api.dto.TaskStateDto;
import com.bolotov.crazy.task.tracker.store.entities.TaskEntity;
import com.bolotov.crazy.task.tracker.store.entities.TaskStateEntity;
import org.springframework.stereotype.Component;

@Component
public class TaskStateDtoFactory {
    public TaskStateDto makeTaskStateDto(TaskStateEntity taskStateEntity) {
        return TaskStateDto.builder()
                .id(taskStateEntity.getId())
                .name(taskStateEntity.getName())
                .createdAt(taskStateEntity.getCreatedAt())
                .ordinal(taskStateEntity.getOrdinal())
                .build();

    }
}
