package com.example.logWatcher.services;

import com.example.logWatcher.entities.User;

import java.io.IOException;

public interface LogService {

    void streamLastTenLines(String id) throws IOException;

    long countTotalLines(String filePath) throws IOException;

    void streamUpdates() throws IOException;

    User subscribeUser(String id);
}
