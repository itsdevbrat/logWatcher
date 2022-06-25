package com.example.logWatcher.services.impl;

import com.example.logWatcher.entities.User;
import com.example.logWatcher.services.LogService;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class LogServiceImpl implements LogService {

    Map<String, User> users = new HashMap<>();
    private static final String FILE_PATH = "C:\\Users\\Subrat_PC\\Desktop\\logs.txt";
    private final AtomicBoolean streamStarted = new AtomicBoolean(false);


    @Override
    public User subscribeUser(String id) {
        users.putIfAbsent(id, new User(id, 0, new SseEmitter()));
        return users.get(id);
    }

    public void streamLastTenLines(String id) throws IOException {
        User currentUser = users.get(id);
        if (currentUser.getLastLineRead() == 0) {
            long totalLines = countTotalLines(FILE_PATH);
            Stream<String> linesStream = Files.lines(Path.of(FILE_PATH));
            linesStream.skip(totalLines - 10).forEach(line -> {
                try {
                    currentUser.getSseEmitter().send(line);
                } catch (IOException e) {
                    currentUser.getSseEmitter().completeWithError(e);
                }
            });
            currentUser.setLastLineRead(totalLines);
        }

    }

    public long countTotalLines(String filePath) throws IOException {
        return Files.lines(Path.of(FILE_PATH)).count();
    }


    public void streamUpdates() throws IOException {
        if (!streamStarted.get()) {
            Executors.newSingleThreadExecutor().submit((Runnable) () -> {

                Path filePath = Path.of(FILE_PATH);
                FileTime lastModifiedTime = null;
                try {
                    lastModifiedTime = Files.getLastModifiedTime(filePath);
                    while (true) {
                        if (Files.getLastModifiedTime(filePath).compareTo(lastModifiedTime) != 0) {
                            lastModifiedTime = Files.getLastModifiedTime(filePath);
                            for (String userId : users.keySet()) {
                                User currentUser = users.get(userId);
                                List<String> lines = readLines(currentUser.getLastLineRead());
                                for (String line : lines) {
                                    currentUser.getSseEmitter().send(line);
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            streamStarted.set(true);
        }
    }

    private List<String> readLines(long lastLineRead) throws IOException {
        Stream<String> linesStream = Files.lines(Path.of(FILE_PATH));
        return linesStream.skip(lastLineRead).collect(Collectors.toList());
    }


}
