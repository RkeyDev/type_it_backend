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

    public DatabaseTable getDatabaseTableName() {
    switch (this) {
        case ENGLISH: return DatabaseTable.ENGLISH;
        case HEBREW:  return DatabaseTable.HEBREW;
        case GERMAN:  return DatabaseTable.GERMAN;
        default: throw new IllegalArgumentException("No matching table for language " + this);
    }
}

}
