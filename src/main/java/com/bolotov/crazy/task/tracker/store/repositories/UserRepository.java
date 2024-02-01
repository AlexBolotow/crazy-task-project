package com.bolotov.crazy.task.tracker.store.repositories;

import com.bolotov.crazy.task.tracker.store.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByName(String username);
}
