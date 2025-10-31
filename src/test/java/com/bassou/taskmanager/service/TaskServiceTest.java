package com.bassou.taskmanager.service;

import com.bassou.taskmanager.model.Task;
import com.bassou.taskmanager.repository.TaskRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService Test Unit")
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private Task testTask;

    @BeforeEach
    void setUp() {
        testTask = new Task();
        testTask.setId(1L);
        testTask.setTitle("Test title");
        testTask.setDescription("Test description");
        testTask.setPriority(Task.Priority.MEDIUM);
        testTask.setCompleted(false);
    }
    @Test
    @DisplayName("Devrait creer une nouvelle tache ")
    void shouldCreateTask() {

        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        Task createdTask = taskService.createTask(testTask);

        assertNotNull(createdTask);
        assertEquals(testTask.getId(), createdTask.getId());
        assertEquals(testTask.getTitle(), createdTask.getTitle());
        assertEquals(testTask.getDescription(), createdTask.getDescription());
        assertEquals(testTask.getPriority(), createdTask.getPriority());
        assertEquals(testTask.isCompleted(), createdTask.isCompleted());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("Devrait recuperer tous les taches")
    void shouldGetAllTask() {

        Task task2 = new Task();
        task2.setId(1L);
        task2.setTitle("Task 2");

        List<Task> tasks = Arrays.asList(task2, testTask);
        when(taskRepository.findAllByOrderByCreatedAtDesc()).thenReturn(tasks);

        List<Task> allTasks = taskService.getAllTasks();

        assertNotNull(allTasks);
        assertEquals(2, allTasks.size());
        verify(taskRepository, times(1)).findAllByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("Recuperer une tache par ID")
    void shouldGetTaskById() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        Optional<Task> foundTask = taskService.getTaskById(1L);
        assertTrue(foundTask.isPresent());
        assertEquals(testTask.getId(), foundTask.get().getId());
        verify(taskRepository, times(1)).findById(1L);
    }
    @Test
    @DisplayName("Devrait retourner Optional.empty pour un ID inexistant")
    void shouldNotGetTaskById() {

        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Task> foundTask = taskService.getTaskById(99L);

        assertFalse(foundTask.isPresent());
        verify(taskRepository, times(1)).findById(99L);
    }
    @Test
    @DisplayName("Devrait mettre à jour une tâche existante")
    void shouldUpdateTask() {
        Task updateTask = new  Task();
        updateTask.setTitle("Update title");
        updateTask.setDescription("Update description");
        updateTask.setPriority(Task.Priority.HIGH);
        updateTask.setCompleted(true);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        Task result = taskService.updateTask(1L, updateTask);

        assertEquals("Update title", result.getTitle());
        assertEquals("Update description", result.getDescription());
        assertEquals(Task.Priority.HIGH, result.getPriority());
        assertTrue(result.isCompleted());
        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).save(any(Task.class));
    }
    @Test
    @DisplayName("Devrait lever une exception lors de la mise à jour d'une tâche inexistante")
    void shouldThrowExceptionWhenUpdatingNonExistentTask(){

        when(taskRepository.findById(990L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> taskService.updateTask(990L, testTask));
        verify(taskRepository, times(1)).findById(990L);
        verify(taskRepository,never()).save(any(Task.class));
    }
    @Test
    @DisplayName("Devrait basculer le statut de complétion")
    void shouldToggleTaskCompletion() {

        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        Task result = taskService.toggleTaskCompletion(1L);
        assertTrue(result.isCompleted());
        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("Devrait supprimer une tâche existante")
    void shouldDeleteTask() {
        when(taskRepository.existsById(1L)).thenReturn(true);
        doNothing().when(taskRepository).deleteById(1L);

        taskService.deleteTask(1L);
        verify(taskRepository, times(1)).existsById(1L);
        verify(taskRepository, times(1)).deleteById(1L);
    }
}
