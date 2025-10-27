package com.javarush.lepeshinskij.util;

public class PasswordEncoder {
    
    public String encode(String rawPassword) {
        return rawPassword;
    }
    
    public boolean matches(String rawPassword, String storedPassword) {
        return rawPassword != null && rawPassword.equals(storedPassword);
    }
}
