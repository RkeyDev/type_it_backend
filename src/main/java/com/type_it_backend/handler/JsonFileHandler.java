package com.type_it_backend.handler;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.type_it_backend.enums.JsonFilePath;

public class JsonFileHandler {
    private JsonNode root;

    public JsonFileHandler(JsonFilePath filePath) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.root = mapper.readTree(new File(filePath.getPath()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load JSON file: " + filePath.getPath(), e);
        }
    }

    
    public JsonFileHandler(JsonNode root) {
        this.root = root;
    }

    public JsonNode getValue(String key){
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
