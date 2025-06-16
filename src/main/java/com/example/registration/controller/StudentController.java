package com.example.registration.controller;

// Add this new import
import com.example.registration.dto.StudentResponseDTO;
import com.example.registration.model.Student;
import com.example.registration.service.StudentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors; // Add this import

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping
    public ResponseEntity<Student> createStudent(@RequestBody Student student) {
        Student createdStudent = studentService.createStudent(student);
    // old way of returning a response
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStudent);
    }


    // list of DTOs of Students
    @GetMapping
    public List<StudentResponseDTO> getAllStudents() {
        return studentService.getAllStudents().stream()
                .map(StudentResponseDTO::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentResponseDTO> getStudentById(@PathVariable Integer id) {
        Student student = studentService.getStudentById(id);
        return ResponseEntity.ok(new StudentResponseDTO(student));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentResponseDTO> updateStudent(@PathVariable Integer id, @RequestBody Student updatedStudentDetails) {
        Student updatedStudent = studentService.updateStudent(id, updatedStudentDetails);

        // convert the updated entity to a DTO before returning
        StudentResponseDTO responseDto = new StudentResponseDTO(updatedStudent);

        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Integer id) {
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }
}