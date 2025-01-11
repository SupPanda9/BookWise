package com.bookwise.backend.model;

import lombok.Data;

import java.util.List;

@Data
public class User {
    private String id;
    private String username;
    private String email;
    private String password;
    private List<String> roles;
    private boolean enabled;

    private List<String> readBooks; // Updated to store only book IDs
    private List<String> collections;

    private Preferences preferences;

    @Data
    public static class Preferences {
        private List<Genre> genres;

        @Data
        public static class Genre {
            private String genre;
            private String lastActive;
        }
    }
}
