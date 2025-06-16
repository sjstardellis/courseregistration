package com.example.registration.service;

import com.example.registration.dto.RegistrationResponseDTO;
import com.example.registration.model.Course;
import com.example.registration.model.Registration;
import com.example.registration.model.Student;
import com.example.registration.repository.CourseRepository;
import com.example.registration.repository.RegistrationRepository;
import com.example.registration.repository.StudentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RegistrationService {

    private final RegistrationRepository registrationRepo;
    private final StudentRepository studentRepo;
    private final CourseRepository courseRepo;

    public RegistrationService(RegistrationRepository registrationRepo, StudentRepository studentRepo, CourseRepository courseRepo) {
        this.registrationRepo = registrationRepo;
        this.studentRepo = studentRepo;
        this.courseRepo = courseRepo;
    }


    // Create a registration from a DTO
    public RegistrationResponseDTO createRegistration(RegistrationResponseDTO requestDto) {
        // finding student and course from database based on their ids
        Student student = studentRepo.findById(requestDto.getStudentId())
                .orElseThrow(() -> new EntityNotFoundException("Student not found with id: " + requestDto.getStudentId()));

        Course course = courseRepo.findById(requestDto.getCourseId())
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + requestDto.getCourseId()));

        // create new registration entity
        Registration registration = new Registration();

        // from the Student and Course objects, set the registrations student and course
        registration.setStudent(student);
        registration.setCourse(course);

        // set the fields
        registration.setStudentName(student.getName());
        registration.setStudentEmail(student.getEmail());
        registration.setCourseName(course.getTitle());

        // save the populated entity to the database
        Registration savedRegistration = registrationRepo.save(registration);

        // return the DTO
        return new RegistrationResponseDTO(savedRegistration);
    }


    // Gets all registrations
    public List<RegistrationResponseDTO> getAllRegistrations() {
        return registrationRepo.findAll().stream()
                .map(RegistrationResponseDTO::new)
                .collect(Collectors.toList());
    }

    // Get all registrations by student id
    public List<RegistrationResponseDTO> getAllRegistrationsByStudentId(Integer studentId) {
        // check if a student exists
        if (!studentRepo.existsById(studentId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found with id: " + studentId);
        }

        // fetch the registrations by the student id and create a list of DTOs as the response
        return registrationRepo.findByStudentId(studentId).stream()
                .map(RegistrationResponseDTO::new)
                .collect(Collectors.toList());
    }

    // Gets registration by id
    public RegistrationResponseDTO getRegistrationById(Integer id) {
        Registration registration = registrationRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registration not found with id: " + id));
        return new RegistrationResponseDTO(registration);
    }

    // Delete registration by id
    public void deleteRegistration(Integer id) {

        // registration must exist
        if (!registrationRepo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Registration not found with id: " + id);
        }
        registrationRepo.deleteById(id);
    }
}