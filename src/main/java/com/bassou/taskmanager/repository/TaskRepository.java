package com.bassou.taskmanager.repository;

import jakarta.annotation.Priority;
import org.springframework.data.jpa.repository.JpaRepository;
import com.bassou.taskmanager.model.Task;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository

public interface TaskRepository extends JpaRepository<Task, Long> {
    // Recherche par statut
    List<Task> findByCompleted(boolean completed);
    // Recherche par priorité
    List<Task> findByPriority(Task.Priority priority);
    // Recherche par titre (insensible à la casse)
    List<Task> findByTitleContainingIgnoreCase(String title);
    // Tri par date de création décroissante
    List<Task> findAllByOrderByCreatedAtDesc();
    // Tâches non complétées triées par priorité
    List<Task> findByCompletedOrderByPriorityDesc(boolean completed);
}
