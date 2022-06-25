package com.example.logWatcher.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Data
@AllArgsConstructor
public class User {

    String id;
    long lastLineRead;
    SseEmitter sseEmitter;

}
