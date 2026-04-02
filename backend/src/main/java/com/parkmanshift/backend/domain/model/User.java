package com.parkmanshift.backend.domain.model;

import java.util.UUID;

public class User {
    private UUID id;
    private String username;
    private String fullName;
    private String password;
    private UserRole role;
    private String checkInCode;

    public User() {}

    public User(UUID id, String username, String fullName, String password, UserRole role, String checkInCode) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.password = password;
        this.role = role;
        this.checkInCode = checkInCode;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public String getCheckInCode() { return checkInCode; }
    public void setCheckInCode(String checkInCode) { this.checkInCode = checkInCode; }
}
