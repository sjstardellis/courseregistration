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

    private TestRestTemplate authenticationTemplate;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @BeforeEach
    void setupAuthenticatedUser() {
        Student authUser = new Student();
        authUser.setName("TestStudent");
        authUser.setEmail("TestStudentEmail@gmail.com");
        authUser.setPassword("password");


        // this endpoint is public, no authentication
        restTemplate.postForEntity("/api/students", authUser, Student.class);

        authenticationTemplate = restTemplate.withBasicAuth("TestStudentEmail@gmail.com", "password");
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

        ResponseEntity<Course> response = authenticationTemplate.postForEntity("/api/courses", newCourse, Course.class);

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

        ResponseEntity<Course> response = authenticationTemplate.getForEntity("/api/courses/" + course.getId(), Course.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(course.getId(), response.getBody().getId());
    }

    @Test
    @DisplayName("GET /api/courses - Return all courses")
    void getAllCourses() {
        courseRepository.save(new Course());
        courseRepository.save(new Course());

        ResponseEntity<List<Course>> response = authenticationTemplate.exchange(
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

        ResponseEntity<Course> response = authenticationTemplate.exchange(
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
    @DisplayName("DELETE /api/courses/{id} - Delete an existing course")
    void deleteCourse() {
        Course course = courseRepository.save(new Course());

        ResponseEntity<Void> response = authenticationTemplate.exchange(
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
        Student student = studentRepository.findByEmail("TestStudentEmail@gmail.com").orElseThrow();
        Course course = courseRepository.save(new Course());

        Registration registration = new Registration();
        registration.setStudent(student);
        registration.setCourse(course);
        registrationRepository.save(registration);

        ResponseEntity<Void> response = authenticationTemplate.exchange(
                "/api/courses/" + course.getId(),
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(courseRepository.existsById(course.getId()));
    }
}