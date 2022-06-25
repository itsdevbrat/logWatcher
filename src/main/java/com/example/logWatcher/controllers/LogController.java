package com.example.logWatcher.controllers;

import com.example.logWatcher.services.LogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@RestController
@RequestMapping("/log")
public class LogController {

    final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @GetMapping("/{id}")
    public SseEmitter subscribe(@PathVariable String id) throws IOException {
        SseEmitter sseEmitter = logService.subscribeUser(id).getSseEmitter();
        logService.streamLastTenLines(id);
        logService.streamUpdates();
        return sseEmitter;

    }

}
