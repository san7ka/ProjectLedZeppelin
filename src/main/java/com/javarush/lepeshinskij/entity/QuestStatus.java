package com.javarush.lepeshinskij.entity;

public enum QuestStatus {
    DRAFT("DRAFT", "Черновик"),
    PUBLISHED("PUBLISHED", "Опубликован");
    
    private final String value;
    private final String displayName;
    
    QuestStatus(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }
    
    public String getValue() {
        return value;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return value;
    }
}
