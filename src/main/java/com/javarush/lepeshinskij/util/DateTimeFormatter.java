package com.javarush.lepeshinskij.util;

import java.time.LocalDateTime;

public class DateTimeFormatter {
    
    private static final java.time.format.DateTimeFormatter DEFAULT_FORMATTER = 
            java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy");
    
    private static final java.time.format.DateTimeFormatter FULL_FORMATTER = 
            java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    
    public static String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DEFAULT_FORMATTER);
    }
    
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(FULL_FORMATTER);
    }
    
    public static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(java.time.format.DateTimeFormatter.ofPattern(pattern));
    }
}
