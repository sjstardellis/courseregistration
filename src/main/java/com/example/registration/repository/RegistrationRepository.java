package com.example.registration.repository;

import com.example.registration.model.Course;
import com.example.registration.model.Registration;
import com.example.registration.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
// Registration - this repository manages the Registration entity
// Integer - the primary key of the Registration entity (registrationID)
public interface RegistrationRepository extends JpaRepository<Registration, Integer> { //


    // find all registrations associated with Student object
    List<Registration> findByStudent(Student student); //
    // find all registrations associated with Course object
    List<Registration> findByCourse(Course course);   //

    // method to check if a registration exists based on student ID
    boolean existsByStudentId(Integer studentId);
    // method to check if a registration exists based on course ID
    boolean existsByCourseId(Integer courseId);
}