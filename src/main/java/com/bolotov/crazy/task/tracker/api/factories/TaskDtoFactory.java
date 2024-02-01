package com.bolotov.crazy.task.tracker.api.factories;

import com.bolotov.crazy.task.tracker.api.dto.TaskDto;
import com.bolotov.crazy.task.tracker.store.entities.TaskEntity;
import org.springframework.stereotype.Component;

@Component
public class TaskDtoFactory {
    public TaskDto makeTaskDto(TaskEntity taskEntity) {
        return TaskDto.builder()
                .id(taskEntity.getId())
                .name(taskEntity.getName())
                //.createdAt(taskEntity.getCreatedAt())
                .description(taskEntity.getDescription())
                .build();
    }
}
