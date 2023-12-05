package com.bolotov.crazy.task.tracker.store.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE) //?
@Entity
@Table(name = "project")
public class ProjectEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    Long id;

    @Column(unique = true)
    String name;

    @Builder.Default
    Instant createdAt = Instant.now();

    @Builder.Default
    @OneToMany
    @JoinColumn(name = "project_id", referencedColumnName = "id")
    List<TaskStateEntity> taskStates = new ArrayList<>();
}
