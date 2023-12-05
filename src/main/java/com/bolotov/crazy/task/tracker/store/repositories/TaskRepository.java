package com.bolotov.crazy.task.tracker.store.repositories;

import com.bolotov.crazy.task.tracker.store.entities.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, Long> {
}
