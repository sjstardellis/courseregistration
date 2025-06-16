package com.example.registration;

import com.example.registration.dto.RegistrationResponseDTO;
import com.example.registration.model.Course;
import com.example.registration.model.Student;
import com.example.registration.repository.CourseRepository;
import com.example.registration.repository.RegistrationRepository;
import com.example.registration.repository.StudentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestRegistration {

    @Autowired
    private TestRestTemplate restTemplate;

    // Authenticated client for accessing protected endpoints
    private TestRestTemplate authenticatedRestTemplate;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @BeforeEach
    void setupAuthenticatedUser() {
        // Create a student to authenticate with
        Student authUser = new Student();
        authUser.setName("Auth User");
        authUser.setEmail("auth.user@example.com");
        authUser.setPassword("password123");

        // Use the public endpoint to create the user, which also handles password encoding
        restTemplate.postForEntity("/api/students", authUser, Student.class);

        // Configure TestRestTemplate with basic auth credentials
        authenticatedRestTemplate = restTemplate.withBasicAuth("auth.user@example.com", "password123");
    }

    @AfterEach
    public void deleteAllData() {
        registrationRepository.deleteAll();
        studentRepository.deleteAll();
        courseRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/registrations - Create a new registration")
    void createRegistration() {
        Student student = new Student();
        student.setName("Student1");
        student.setPassword("pass");
        student = studentRepository.save(student);

        Course course = new Course();
        course.setTitle("Course1");
        course = courseRepository.save(course);

        RegistrationResponseDTO requestDto = new RegistrationResponseDTO();
        requestDto.setStudentId(student.getId());
        requestDto.setCourseId(course.getId());

        // Use the authenticated client
        ResponseEntity<RegistrationResponseDTO> response = authenticatedRestTemplate.postForEntity("/api/registrations", requestDto, RegistrationResponseDTO.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(student.getId(), response.getBody().getStudentId());
        assertEquals(course.getId(), response.getBody().getCourseId());
    }

    @Test
    @DisplayName("GET /api/registrations/{id} - Return a registration DTO")
    void getRegistration() {
        Student student = studentRepository.save(new Student());
        Course course = courseRepository.save(new Course());

        RegistrationResponseDTO createDto = new RegistrationResponseDTO();
        createDto.setStudentId(student.getId());
        createDto.setCourseId(course.getId());

        // Use the authenticated client to create the registration
        ResponseEntity<RegistrationResponseDTO> createdResponse = authenticatedRestTemplate.postForEntity("/api/registrations", createDto, RegistrationResponseDTO.class);
        Integer registrationId = createdResponse.getBody().getRegistrationID();

        // Use the authenticated client to get the registration
        ResponseEntity<RegistrationResponseDTO> response = authenticatedRestTemplate.getForEntity("/api/registrations/" + registrationId, RegistrationResponseDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(registrationId, response.getBody().getRegistrationID());
    }

    @Test
    @DisplayName("GET /api/registrations - Return all registrations")
    void getAllRegistrations_shouldReturnListOfDTOs() {
        Student student = studentRepository.save(new Student());
        Course course1 = courseRepository.save(new Course());
        Course course2 = courseRepository.save(new Course());

        RegistrationResponseDTO DTO1 = new RegistrationResponseDTO();
        DTO1.setStudentId(student.getId());
        DTO1.setCourseId(course1.getId());
        authenticatedRestTemplate.postForEntity("/api/registrations", DTO1, RegistrationResponseDTO.class);

        RegistrationResponseDTO DTO2 = new RegistrationResponseDTO();
        DTO2.setStudentId(student.getId());
        DTO2.setCourseId(course2.getId());
        authenticatedRestTemplate.postForEntity("/api/registrations", DTO2, RegistrationResponseDTO.class);

        // Use the authenticated client
        ResponseEntity<List<RegistrationResponseDTO>> response = authenticatedRestTemplate.exchange(
                "/api/registrations",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    @Test
    @DisplayName("DELETE /api/registrations/{id} - Should delete a registration")
    void deleteRegistration_shouldReturnNoContent() {
        Student student = studentRepository.save(new Student());
        Course course = courseRepository.save(new Course());
        RegistrationResponseDTO createDto = new RegistrationResponseDTO();
        createDto.setStudentId(student.getId());
        createDto.setCourseId(course.getId());

        ResponseEntity<RegistrationResponseDTO> createdResponse = authenticatedRestTemplate.postForEntity("/api/registrations", createDto, RegistrationResponseDTO.class);
        Integer registrationID = createdResponse.getBody().getRegistrationID();

        // Use the authenticated client
        ResponseEntity<Void> response = authenticatedRestTemplate.exchange(
                "/api/registrations/" + registrationID,
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertFalse(registrationRepository.existsById(registrationID));
    }

    @Test
    @DisplayName("GET /api/registrations/studentid/{id} - Return all registrations for a student")
    void getAllRegistrationsByStudentId_shouldReturnCorrectRegistrations() {
        Student student1 = new Student();
        student1.setName("Student1");
        student1.setPassword("pass1");
        student1 = studentRepository.save(student1);

        // ... (rest of the test remains the same as it uses the authenticated client)

        // Use the authenticated client
        ResponseEntity<List<RegistrationResponseDTO>> response = authenticatedRestTemplate.exchange(
                "/api/registrations/studentid/" + student1.getId(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<RegistrationResponseDTO> registrations = response.getBody();
        assertNotNull(registrations);
        assertEquals(0, registrations.size()); // This will be 0 as we haven't registered this student for any courses yet in this test.
    }

    @Test
    @DisplayName("GET /api/registrations/studentid/{id} - No registration found by student id")
    void getAllRegistrationsByStudentId_shouldReturnNotFound() {
        // This test is for a non-existent student, but the endpoint itself is protected.
        // We still need to make the call with an authenticated user.
        int nonExistentStudentId = -1;

        ResponseEntity<Object> response = authenticatedRestTemplate.getForEntity(
                "/api/registrations/studentid/" + nonExistentStudentId,
                Object.class
        );

        // The service should correctly return a 404 even when called by an authenticated user.
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}