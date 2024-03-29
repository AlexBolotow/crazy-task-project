package com.bolotov.crazy.task.tracker.store.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "task")
public class TaskEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String name;

    @Builder.Default
    private Instant createdAt = Instant.now();

    @Builder.Default
    private String description = "";

    @ManyToOne
    TaskStateEntity task_state;
}
