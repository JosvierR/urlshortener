package edu.pucmm.models;

import org.bson.types.ObjectId;

public class User {
    private ObjectId id;
    private String username;
    private String password; // In production, hash the password!
    private String role;     // "admin" or "user"

    public User(String username, String password, String role) {
        this.id = new ObjectId();
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public ObjectId getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
}
