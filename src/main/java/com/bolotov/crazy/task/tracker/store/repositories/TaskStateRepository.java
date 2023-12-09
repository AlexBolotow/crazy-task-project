package com.bolotov.crazy.task.tracker.store.repositories;

import com.bolotov.crazy.task.tracker.store.entities.TaskStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskStateRepository extends JpaRepository<TaskStateEntity, Long> {

    Optional<TaskStateEntity> findTaskStateEntityByRightTaskStateIsNullAndProjectId(Long projectId);

    Optional<TaskStateEntity> findTaskStateByProjectIdAndNameContainsIgnoreCase(Long projectId, String taskStateName);

}
