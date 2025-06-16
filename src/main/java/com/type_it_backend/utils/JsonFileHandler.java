package com.type_it_backend.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.type_it_backend.enums.JsonFilePath;

public class JsonFileHandler {
    private JsonNode root;

    public JsonFileHandler(JsonFilePath filePath) {
        throw new UnsupportedOperationException("Method not implemented yet");
    }
    
    public JsonFileHandler(JsonNode root) {
        this.root = root;
    }

    public Object getValue() {
        throw new UnsupportedOperationException("Method not implemented yet");
    }
}
