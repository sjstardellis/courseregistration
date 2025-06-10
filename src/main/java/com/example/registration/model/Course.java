package com.example.registration.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Course {

    // table attributes
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer course_id;
    private String title;
    private String description;

    // getters
    public Integer getId() {
        return course_id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    // setters
    public void setId(Integer course_id) {
        this.course_id = course_id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}