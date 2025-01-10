package com.bookwise.backend.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class User {
    private String id; // Unique identifier for the user
    private String username;
    private String email;
    private boolean enabled;
    private List<String> collections; // List of collection IDs
    private Preferences preferences; // Nested preferences for genres/authors
    private List<BookInfo> readBooks; // Books the user has read
    private String password; // Hashed password
    private List<String> roles;

    @Getter
    @Setter
    public static class Preferences {
        private List<Genre> genres; // List of genres or authors

        @Getter
        @Setter
        public static class Genre {
            private String genre; // Genre name
            private String lastActive; // Timestamp when last active (optional for registration)
        }
    }

    @Getter
    @Setter
    public static class BookInfo {
        private String bookId;
        private String isbn;
        private String title;
    }
}
