package com.example.registration;

import com.example.registration.model.Course;
import com.example.registration.model.Registration;
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
import org.springframework.http.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestCourse {

    @Autowired
    private TestRestTemplate restTemplate;

    // Authenticated client for accessing protected endpoints
    private TestRestTemplate authenticatedRestTemplate;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentRepository studentRepository;

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
    void deleteAllData() {
        registrationRepository.deleteAll();
        studentRepository.deleteAll();
        courseRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/courses - Create a new course")
    void createCourse() {
        Course newCourse = new Course();
        newCourse.setTitle("Intro to Spring Boot");
        newCourse.setDescription("A course on how Spring Boot works.");

        // Use the authenticated client
        ResponseEntity<Course> response = authenticatedRestTemplate.postForEntity("/api/courses", newCourse, Course.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Intro to Spring Boot", response.getBody().getTitle());
    }

    @Test
    @DisplayName("GET /api/courses/{id} - Return a course")
    void getCourse() {
        Course course = new Course();
        course.setTitle("Test Course");
        course = courseRepository.save(course);

        // Use the authenticated client
        ResponseEntity<Course> response = authenticatedRestTemplate.getForEntity("/api/courses/" + course.getId(), Course.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(course.getId(), response.getBody().getId());
    }

    @Test
    @DisplayName("GET /api/courses - Return all courses")
    void getAllCourses() {
        courseRepository.save(new Course());
        courseRepository.save(new Course());

        // Use the authenticated client
        ResponseEntity<List<Course>> response = authenticatedRestTemplate.exchange(
                "/api/courses",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    @Test
    @DisplayName("PUT /api/courses/{id} - Update an existing course")
    void updateCourse() {
        Course course = new Course();
        course.setTitle("Test Course");
        course = courseRepository.save(course);

        Course updatedDetails = new Course();
        updatedDetails.setTitle("New Test Course");

        HttpEntity<Course> requestEntity = new HttpEntity<>(updatedDetails);

        // Use the authenticated client
        ResponseEntity<Course> response = authenticatedRestTemplate.exchange(
                "/api/courses/" + course.getId(),
                HttpMethod.PUT,
                requestEntity,
                Course.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("New Test Course", response.getBody().getTitle());
    }

    @Test
    @DisplayName("DELETE /api/courses/{id} - Should delete a course")
    void deleteCourse() {
        Course course = courseRepository.save(new Course());

        // Use the authenticated client
        ResponseEntity<Void> response = authenticatedRestTemplate.exchange(
                "/api/courses/" + course.getId(),
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertFalse(courseRepository.existsById(course.getId()));
    }

    @Test
    @DisplayName("DELETE /api/courses/{id} - Return 409 if the user is registered for a course")
    void deleteCourse_Registered() {
        // Find the auth user created in @BeforeEach
        Student student = studentRepository.findByEmail("auth.user@example.com").orElseThrow();
        Course course = courseRepository.save(new Course());

        Registration registration = new Registration();
        registration.setStudent(student);
        registration.setCourse(course);
        registrationRepository.save(registration);

        // Use the authenticated client
        ResponseEntity<Void> response = authenticatedRestTemplate.exchange(
                "/api/courses/" + course.getId(),
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(courseRepository.existsById(course.getId()));
    }
}