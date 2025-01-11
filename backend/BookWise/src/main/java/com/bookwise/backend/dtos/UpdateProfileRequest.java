package com.bookwise.backend.dtos;

import lombok.Data;
import java.util.List;

@Data
public class UpdateProfileRequest {
    private String username;
    private String email;
    private String password;
    private List<String> preferences;
    private Boolean isPublic;

    private List<String> booksToAdd;
    private List<String> booksToRemove;
}

