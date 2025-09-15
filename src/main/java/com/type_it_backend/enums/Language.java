package com.type_it_backend.enums;

public enum Language {
    ENGLISH("english"),
    HEBREW("hebrew"),
    GERMAN("german")
    ;

    private final String language;

    Language(String language) {
        this.language = language;
    }

    public String getLanguage() {
        return language;
    }
}
