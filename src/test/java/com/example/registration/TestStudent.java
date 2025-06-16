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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestStudent {

    @Autowired
    private TestRestTemplate restTemplate;

    // This template will be configured with basic auth credentials
    private TestRestTemplate authenticatedRestTemplate;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    // Setup an authenticated user before each test
    @BeforeEach
    void setupAuthenticatedUser() {
        // Create a student with a known password to be used for authentication
        Student authUser = new Student();
        authUser.setName("Auth User");
        authUser.setEmail("auth.user@example.com");
        authUser.setPassword("password123"); // Set a plain text password

        // Use the public endpoint to create the user, which also handles password encoding
        restTemplate.postForEntity("/api/students", authUser, Student.class);

        // Configure a new TestRestTemplate with basic auth credentials
        authenticatedRestTemplate = restTemplate.withBasicAuth("auth.user@example.com", "password123");
    }


    // removes all data before and after testing
    @AfterEach
    public void deleteAllData() {
        registrationRepository.deleteAll();
        studentRepository.deleteAll();
        courseRepository.deleteAll();
    }


    // This endpoint is public, so it doesn't need authentication.
    @Test
    @DisplayName("POST /api/students - Create a new student")
    public void createStudent() {
        Student newStudent = new Student();
        newStudent.setName("Test Student");
        newStudent.setEmail("test@example.com");
        newStudent.setPassword("password");

        // Use the unauthenticated restTemplate since this endpoint is permitted for all
        ResponseEntity<Student> response = restTemplate.postForEntity("/api/students", newStudent, Student.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals("Test Student", response.getBody().getName());
        assertEquals("test@example.com", response.getBody().getEmail());
    }

    private static Stream<Arguments> createMultipleStudents() {
        return Stream.of(
                Arguments.of("Jane Smith", "jane.smith@example.com", "pass1"),
                Arguments.of("Peter Jones", "peter.jones@example.com", "pass2"),
                Arguments.of("Mary Brown", "mary.brown@example.com", "pass3")
        );
    }

    @ParameterizedTest
    @MethodSource("createMultipleStudents")
    @DisplayName("POST /api/students - Create multiple new students")
    public void createMultipleStudents(String name, String email, String password) {
        Student newStudent = new Student();
        newStudent.setName(name);
        newStudent.setEmail(email);
        newStudent.setPassword(password);

        // Use the unauthenticated restTemplate for the public creation endpoint
        ResponseEntity<Student> response = restTemplate.postForEntity("/api/students", newStudent, Student.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(name, response.getBody().getName());
        assertTrue(studentRepository.existsById(response.getBody().getId()));
    }

    @Test
    @DisplayName("GET /api/students/{id} - Return a student")
    void getStudentByID() {
        Student student = studentRepository.findByEmail("auth.user@example.com").orElseThrow();

        // Use the authenticated template to access a protected endpoint
        ResponseEntity<Student> response = authenticatedRestTemplate.getForEntity("/api/students/" + student.getId(), Student.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(student.getId(), response.getBody().getId());
    }

    @Test
    @DisplayName("GET /api/students/{id} - Return 404")
    void getStudentByInvalidID() {
        // Use the authenticated template to access a protected endpoint
        ResponseEntity<Student> response = authenticatedRestTemplate.getForEntity("/api/students/-1", Student.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("GET /api/students - Return all students")
    void getAllStudents() {
        // The auth user is already created, let's add one more
        Student student2 = new Student();
        student2.setName("Ben");
        student2.setEmail("ben@example.com");
        student2.setPassword("password");
        studentRepository.save(student2);

        // Use the authenticated template to fetch all students
        ResponseEntity<List<Student>> response = authenticatedRestTemplate.exchange(
                "/api/students",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    @Test
    @DisplayName("PUT /api/students/{id} - Update an existing student")
    void updateStudentByID() {
        Student student = studentRepository.findByEmail("auth.user@example.com").orElseThrow();

        Student updatedDetails = new Student();
        updatedDetails.setName("Updated Name");
        updatedDetails.setEmail("updated.email@example.com");

        HttpEntity<Student> requestEntity = new HttpEntity<>(updatedDetails);

        // Use the authenticated template to update the student
        ResponseEntity<Student> response = authenticatedRestTemplate.exchange("/api/students/" + student.getId(),
                HttpMethod.PUT,
                requestEntity,
                Student.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Updated Name", response.getBody().getName());
        assertEquals("updated.email@example.com", response.getBody().getEmail());
    }

    @Test
    @DisplayName("DELETE /api/students/{id} - Delete a student")
    public void deleteStudent() {
        Student student = new Student();
        student.setName("Deleted Student");
        student.setEmail("delete@example.com");
        student.setPassword("password");
        student = studentRepository.save(student);

        // Use the authenticated template to perform the delete operation
        ResponseEntity<Void> response = authenticatedRestTemplate.exchange(
                "/api/students/" + student.getId(),
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertFalse(studentRepository.existsById(student.getId()));
    }

    @Test
    @DisplayName("DELETE /api/students/{id} - Return 409 if the user is registered for a course")
    void deleteStudent_Registered() {
        Student student = studentRepository.findByEmail("auth.user@example.com").orElseThrow();
        Course course = courseRepository.save(new Course());

        Registration registration = new Registration();
        registration.setStudent(student);
        registration.setCourse(course);
        registrationRepository.save(registration);

        // Use the authenticated template to try to delete the student
        ResponseEntity<Void> response = authenticatedRestTemplate.exchange(
                "/api/students/" + student.getId(),
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(studentRepository.existsById(student.getId()));
    }
}