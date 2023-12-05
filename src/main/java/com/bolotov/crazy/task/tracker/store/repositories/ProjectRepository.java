package com.bolotov.crazy.task.tracker.store.repositories;

import com.bolotov.crazy.task.tracker.store.entities.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {
    
}
