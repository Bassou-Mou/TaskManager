package com.bassou.taskmanager.controller;

import com.bassou.taskmanager.model.Task;
import com.bassou.taskmanager.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@WebMvcTest(TaskController.class)
@DisplayName("TaskController Integration Tests")
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    private Task testTask;

    @BeforeEach
    void setUp() {
        testTask = new Task();
        testTask.setId(1L);
        testTask.setTitle("Test Task");
        testTask.setDescription("Test Description");
        testTask.setPriority(Task.Priority.MEDIUM);
        testTask.setCompleted(false);
    }

    @Test
    @DisplayName("GET /api/tasks - Devrait retourner toutes les tâches")
    void shouldGetAllTasks() throws Exception {
        // Given
        Task task2 = new Task();
        task2.setId(2L);
        task2.setTitle("Task 2");

        when(taskService.getAllTasks()).thenReturn(Arrays.asList(testTask, task2));

        // When & Then
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("Test Task")))
                .andExpect(jsonPath("$[1].title", is("Task 2")));

        verify(taskService, times(1)).getAllTasks();
    }

    @Test
    @DisplayName("GET /api/tasks/{id} - Devrait retourner une tâche par ID")
    void shouldGetTaskById() throws Exception {
        // Given
        when(taskService.getTaskById(1L)).thenReturn(Optional.of(testTask));

        // When & Then
        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Test Task")))
                .andExpect(jsonPath("$.description", is("Test Description")));

        verify(taskService, times(1)).getTaskById(1L);
    }

    @Test
    @DisplayName("GET /api/tasks/{id} - Devrait retourner 404 pour un ID inexistant")
    void shouldReturn404ForNonExistentTask() throws Exception {
        // Given
        when(taskService.getTaskById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/tasks/999"))
                .andExpect(status().isNotFound());

        verify(taskService, times(1)).getTaskById(999L);
    }

    @Test
    @DisplayName("POST /api/tasks - Devrait créer une nouvelle tâche")
    void shouldCreateTask() throws Exception {
        // Given
        Task newTask = new Task();
        newTask.setTitle("New Task");
        newTask.setDescription("New Description");
        newTask.setPriority(Task.Priority.HIGH);

        when(taskService.createTask(any(Task.class))).thenReturn(testTask);

        // When & Then
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newTask)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Test Task")));

        verify(taskService, times(1)).createTask(any(Task.class));
    }

    @Test
    @DisplayName("POST /api/tasks - Devrait retourner 400 pour une tâche invalide")
    void shouldReturn400ForInvalidTask() throws Exception {
        // Given - Tâche avec un titre trop court (minimum 3 caractères)
        Task invalidTask = new Task();
        invalidTask.setTitle("AB"); // Trop court

        // When & Then
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTask)))
                .andExpect(status().isBadRequest());

        verify(taskService, never()).createTask(any(Task.class));
    }

    @Test
    @DisplayName("PUT /api/tasks/{id} - Devrait mettre à jour une tâche")
    void shouldUpdateTask() throws Exception {
        // Given
        Task updatedTask = new Task();
        updatedTask.setTitle("Updated Task");
        updatedTask.setDescription("Updated Description");
        updatedTask.setPriority(Task.Priority.HIGH);

        when(taskService.updateTask(eq(1L), any(Task.class))).thenReturn(updatedTask);

        // When & Then
        mockMvc.perform(put("/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedTask)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Updated Task")));

        verify(taskService, times(1)).updateTask(eq(1L), any(Task.class));
    }

    @Test
    @DisplayName("PUT /api/tasks/{id} - Devrait retourner 404 pour une mise à jour d'une tâche inexistante")
    void shouldReturn404WhenUpdatingNonExistentTask() throws Exception {
        // Given
        when(taskService.updateTask(eq(999L), any(Task.class)))
                .thenThrow(new RuntimeException("Tâche non trouvée"));

        // When & Then
        mockMvc.perform(put("/api/tasks/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testTask)))
                .andExpect(status().isNotFound());

        verify(taskService, times(1)).updateTask(eq(999L), any(Task.class));
    }

    @Test
    @DisplayName("PATCH /api/tasks/{id}/toggle - Devrait basculer le statut de complétion")
    void shouldToggleTaskCompletion() throws Exception {
        // Given
        testTask.setCompleted(true);
        when(taskService.toggleTaskCompletion(1L)).thenReturn(testTask);

        // When & Then
        mockMvc.perform(patch("/api/tasks/1/toggle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed", is(true)));

        verify(taskService, times(1)).toggleTaskCompletion(1L);
    }

    @Test
    @DisplayName("DELETE /api/tasks/{id} - Devrait supprimer une tâche")
    void shouldDeleteTask() throws Exception {
        // Given
        doNothing().when(taskService).deleteTask(1L);

        // When & Then
        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNoContent());

        verify(taskService, times(1)).deleteTask(1L);
    }

    @Test
    @DisplayName("DELETE /api/tasks/{id} - Devrait retourner 404 lors de la suppression d'une tâche inexistante")
    void shouldReturn404WhenDeletingNonExistentTask() throws Exception {
        // Given
        doThrow(new RuntimeException("Tâche non trouvée"))
                .when(taskService).deleteTask(999L);

        // When & Then
        mockMvc.perform(delete("/api/tasks/999"))
                .andExpect(status().isNotFound());

        verify(taskService, times(1)).deleteTask(999L);
    }

    @Test
    @DisplayName("GET /api/tasks/status/{completed} - Devrait filtrer les tâches par statut")
    void shouldGetTasksByStatus() throws Exception {
        // Given
        when(taskService.getTasksByStatus(true)).thenReturn(Arrays.asList(testTask));

        // When & Then
        mockMvc.perform(get("/api/tasks/status/true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(taskService, times(1)).getTasksByStatus(true);
    }

    @Test
    @DisplayName("GET /api/tasks/search - Devrait rechercher des tâches par titre")
    void shouldSearchTasksByTitle() throws Exception {
        // Given
        when(taskService.searchTasksByTitle("Test")).thenReturn(Arrays.asList(testTask));

        // When & Then
        mockMvc.perform(get("/api/tasks/search")
                        .param("title", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", containsString("Test")));

        verify(taskService, times(1)).searchTasksByTitle("Test");
    }
}