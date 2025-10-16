package com.type_it_backend.enums;

public enum DatabaseTable {
    ENGLISH("questions"),
    HEBREW("hebrew_questions"),
    GERMAN("german_questions");

    private final String resourceName;

    DatabaseTable(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getResourceName() {
        return resourceName;
    }
}
