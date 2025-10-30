package com.bassou.taskmanager.controller;

import com.bassou.taskmanager.model.Task;
import com.bassou.taskmanager.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class WebController {

    private final TaskService taskService;

    @Autowired
    public WebController(TaskService taskService) {
        this.taskService = taskService;
    }

    // Page d'accueil avec la liste des tâches
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("tasks", taskService.getAllTasks());
        model.addAttribute("priorities", Task.Priority.values());
        return "index";
    }
}