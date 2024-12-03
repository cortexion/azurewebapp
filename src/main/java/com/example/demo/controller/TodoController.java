package com.example.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.demo.model.Todo;
import com.example.demo.repository.TodoRepository;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest; // Import from Jakarta

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/todos")
public class TodoController {

    private final TodoRepository todoRepository;
    private static final Logger logger = LoggerFactory.getLogger(TodoController.class);

    @Autowired
    private RestTemplate restTemplate;

    public TodoController(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    @GetMapping({ "", "/" })
    public ResponseEntity<Map<String, Iterable<Todo>>> getTodos() {
        logger.info("Fetching all todos!");

        Iterable<Todo> todos = todoRepository.findAll();

        // Wrap the todos in a map with the key "todos"
        Map<String, Iterable<Todo>> response = new HashMap<>();
        response.put("todos", todos);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/test")
    public String getTest() {
        return "This is main!";
    }

    @GetMapping("/message")
    public ResponseEntity<String> getMessage(HttpServletRequest request) {
        // Log the incoming request IP address
        String clientIp = request.getRemoteAddr(); // This should work correctly
        logger.info("Received request from IP: {}", clientIp);

        // Fetch the message from the external endpoint
        String externalMessage = restTemplate.getForObject("http://10.0.2.4/", String.class);

        // Return the fetched message directly as a JSON response
        return ResponseEntity.ok(externalMessage); // This assumes externalMessage is already in JSON format
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Todo createTodo(@RequestBody Todo todo) {
        return todoRepository.save(todo);
    }
}
