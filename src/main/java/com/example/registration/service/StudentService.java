package com.example.registration.service;

import com.example.registration.model.Student;
import com.example.registration.repository.RegistrationRepository;
import com.example.registration.repository.StudentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepo;
    private final RegistrationRepository registrationRepo;

    public StudentService(StudentRepository studentRepo, RegistrationRepository registrationRepo) {
        this.studentRepo = studentRepo;
        this.registrationRepo = registrationRepo;
    }

    // Get all students
    public List<Student> getAllStudents() {
        return studentRepo.findAll();
    }

    // Get student by id
    public Student getStudentById(Integer id) {
        return studentRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found with id: " + id));
    }

    // Create a student
    public Student createStudent(Student student) {
        return studentRepo.save(student);
    }


    // Update a student, changing the name and email both in Student and Registration tables
    public Student updateStudent(Integer id, Student updatedStudentDetails) {
        Student student = getStudentById(id);
        student.setName(updatedStudentDetails.getName());
        student.setEmail(updatedStudentDetails.getEmail());
        return studentRepo.save(student);
    }

    // Delete a student by id
    public void deleteStudent(Integer id) {

        // student must exist
        if (!studentRepo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found with id: " + id);
        }

        // student must not be tied to a registration
        if (registrationRepo.existsByStudentId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot delete student with existing registrations."); //
        }
        studentRepo.deleteById(id);
    }
}