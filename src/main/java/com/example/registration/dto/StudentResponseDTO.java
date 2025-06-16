package com.example.registration.dto;

import com.example.registration.model.Student;

public class StudentResponseDTO {

    // no password field, so that it doesn't get returned in the response
    private Integer id;
    private String name;
    private String email;

    public StudentResponseDTO() {
    }

    public StudentResponseDTO(Student student) {
        this.id = student.getId();
        this.name = student.getName();
        this.email = student.getEmail();
    }

    // getters and setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}