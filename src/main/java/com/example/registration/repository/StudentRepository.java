package com.example.registration.repository;

import com.example.registration.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// Student - this repository manages the Student entity
// Integer - the primary key of the Student entity (studentID)
@Repository
public interface StudentRepository extends JpaRepository<Student, Integer> {
    // finding a potential student by email
    Optional<Student> findByEmail(String email);

}