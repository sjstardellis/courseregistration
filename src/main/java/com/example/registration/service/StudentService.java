package com.example.registration.service;

import com.example.registration.model.Student;
import com.example.registration.repository.RegistrationRepository;
import com.example.registration.repository.StudentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder; // Import PasswordEncoder
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepo;
    private final RegistrationRepository registrationRepo;

    // adding password encoder here
    private final PasswordEncoder passwordEncoder;

    public StudentService(StudentRepository studentRepo, RegistrationRepository registrationRepo, PasswordEncoder passwordEncoder) {
        this.studentRepo = studentRepo;
        this.registrationRepo = registrationRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Student> getAllStudents() {
        return studentRepo.findAll();
    }

    public Student getStudentById(Integer id) {
        return studentRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found with id: " + id));
    }

    public Student createStudent(Student student) {
        // set the password based on the user's request
        student.setPassword(passwordEncoder.encode(student.getPassword()));
        return studentRepo.save(student);
    }

    public Student updateStudent(Integer id, Student updatedStudentDetails) {
        Student student = getStudentById(id);
        student.setName(updatedStudentDetails.getName());
        student.setEmail(updatedStudentDetails.getEmail());

        // check if a new password was provided and encode it
        if (updatedStudentDetails.getPassword() != null && !updatedStudentDetails.getPassword().isEmpty()) {
            student.setPassword(passwordEncoder.encode(updatedStudentDetails.getPassword()));
        }
        return studentRepo.save(student);
    }

    public void deleteStudent(Integer id) {
        if (!studentRepo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found with id: " + id);
        }
        if (registrationRepo.existsByStudentId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot delete student with existing registrations.");
        }
        studentRepo.deleteById(id);
    }
}