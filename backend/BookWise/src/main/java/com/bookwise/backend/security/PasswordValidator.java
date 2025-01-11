package com.bookwise.backend.security;

import java.util.regex.Pattern;

public class PasswordValidator {

    private static final int MIN_LENGTH = 8;
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHARACTER_PATTERN = Pattern.compile("[@#$%!^&*]");
    private static final String[] COMMON_PASSWORDS = {
        "password", "123456", "123456789", "qwerty", "abc123", "password1",
        "12345", "12345678", "111111", "123123", "letmein", "welcome",
        "iloveyou", "admin", "monkey", "sunshine", "football", "charlie",
        "hello", "whatever", "freedom", "password123", "princess", "dragon"
    };

    public static void validatePassword(String password, String username, String email) throws Exception {
        if (password.length() < MIN_LENGTH) {
            throw new Exception("Password must be at least " + MIN_LENGTH + " characters long.");
        }
        if (!UPPERCASE_PATTERN.matcher(password).find()) {
            throw new Exception("Password must contain at least one uppercase letter (A-Z).");
        }
        if (!LOWERCASE_PATTERN.matcher(password).find()) {
            throw new Exception("Password must contain at least one lowercase letter (a-z).");
        }
        if (!DIGIT_PATTERN.matcher(password).find()) {
            throw new Exception("Password must contain at least one digit (0-9).");
        }
        if (!SPECIAL_CHARACTER_PATTERN.matcher(password).find()) {
            throw new Exception("Password must contain at least one special character (@, #, $, etc.).");
        }
        for (String commonPassword : COMMON_PASSWORDS) {
            if (password.equalsIgnoreCase(commonPassword)) {
                throw new Exception("Password is too common. Choose a stronger password.");
            }
        }
        if (username != null && password.toLowerCase().contains(username.toLowerCase())) {
            throw new Exception("Password should not contain parts of the username.");
        }
        if (email != null) {
            String emailLocalPart = email.split("@")[0];
            if (password.toLowerCase().contains(emailLocalPart.toLowerCase())) {
                throw new Exception("Password should not contain parts of the email address.");
            }
        }
    }
}
