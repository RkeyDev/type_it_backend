package com.type_it_backend.enums;

public enum JsonFilePath {
    WORDS_FILE("words.json");

    private final String resourceName;

    JsonFilePath(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getResourceName() {
        return resourceName;
    }
}
