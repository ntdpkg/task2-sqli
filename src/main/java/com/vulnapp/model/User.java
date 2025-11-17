package com.vulnapp.model;

import java.io.Serializable;

public class User implements Serializable {
    private int userId;
    private String username;
    private String email;
    private String role;

    public User() {}

    public User(int userId, String username, String email, String role) {
        this.userId = userId;
        this.username = username;   
        this.email = email;
        this.role = role;
    }

    public int getId() { return userId; }
    public void setId(int userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}