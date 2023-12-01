package com.example.application.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
public class User {

    @Id
    @NotBlank
    @Size(max = 100)
    @Column(unique = true, name = "username")
    private String username; // ใช้ username เป็น primary key

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @NotBlank
    @Size(max = 100)
    private String fullName;

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getFullName() {
        return fullName;
    }

    @NotBlank
    @Size(max = 50)
    @Column(name = "student_id")
    private String studentId;

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    @NotBlank
    @Size(max = 255)
    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @NotBlank
    @Email
    @Size(max = 100)
    @Column(name = "email")
    private String email;

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
    // Getters and Setters
    // ...
}