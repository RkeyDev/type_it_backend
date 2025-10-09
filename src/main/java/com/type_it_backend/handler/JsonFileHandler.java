package com.type_it_backend.handler;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.type_it_backend.enums.JsonFilePath;

public class JsonFileHandler {
    private JsonNode root;

    public JsonFileHandler(JsonFilePath filePath) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath.getResourceName());
            if (inputStream == null) {
                throw new RuntimeException("Resource not found: " + filePath.getResourceName());
            }
            this.root = mapper.readTree(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load JSON file: " + filePath.getResourceName(), e);
        }
    }

    public String[] getAllKeys() {
        try {
            java.util.Iterator<String> iterator = root.fieldNames();
            java.util.List<String> keys = new java.util.ArrayList<>();
            while (iterator.hasNext()) {
                keys.add(iterator.next());
            }
            return keys.toArray(String[]::new);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get all keys from JSON", e);
        }
    }

    public JsonFileHandler(JsonNode root) {
        this.root = root;
    }

    public JsonNode getValue(String key) {
        try {
            return root.get(key);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get value for key: " + key, e);
        }
    }

    public JsonNode getValue() {
        return root;
    }
}
