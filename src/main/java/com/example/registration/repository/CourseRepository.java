package com.example.registration.repository;

import com.example.registration.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// Registration - this repository manages the Registration entity
// Long - the primary key of the Registration entity (registrationID)
@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {}
