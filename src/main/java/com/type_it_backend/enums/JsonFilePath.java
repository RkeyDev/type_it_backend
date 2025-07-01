package com.type_it_backend.enums;

public enum JsonFilePath {
    WORDS_FILE("src/main/resources/words.json");

    private final String path;

    JsonFilePath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
