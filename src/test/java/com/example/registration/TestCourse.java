package com.example.registration;

import com.example.registration.model.Course;
import com.example.registration.model.Registration;
import com.example.registration.model.Student;
import com.example.registration.repository.CourseRepository;
import com.example.registration.repository.RegistrationRepository;
import com.example.registration.repository.StudentRepository;
import org.junit.jupiter.api.AfterEach;
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

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private RegistrationRepository registrationRepository;


    // removes all data before and after testing

    @AfterEach
    void deleteAllData() {
        registrationRepository.deleteAll();
        studentRepository.deleteAll();
        courseRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/courses - Create a new course")
    void createCourse() {
        // new course
        Course newCourse = new Course();
        newCourse.setTitle("Intro to Spring Boot");
        newCourse.setDescription("A course on how Spring Boot works.");

        // create course response
        ResponseEntity<Course> response = restTemplate.postForEntity("/api/courses", newCourse, Course.class);

        // assert response is valid
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Intro to Spring Boot", response.getBody().getTitle());
        assertEquals("A course on how Spring Boot works.", response.getBody().getDescription());
    }

    @Test
    @DisplayName("GET /api/courses/{id} - Return a course")
    void getCourse() {
        // create new course
        Course course = new Course();
        course.setTitle("Test Course");
        course = courseRepository.save(course);

        // create response
        ResponseEntity<Course> response = restTemplate.getForEntity("/api/courses/" + course.getId(), Course.class);

        // assert that the correct course is given
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(course.getId(), response.getBody().getId());
    }

    @Test
    @DisplayName("GET /api/courses - Return all courses")
    void getAllCourses() {
        // two courses saved
        courseRepository.save(new Course());
        courseRepository.save(new Course());

        // create response
        ResponseEntity<List<Course>> response = restTemplate.exchange(
                "/api/courses",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // assert that we are given 2 courses back
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    @Test
    @DisplayName("PUT /api/courses/{id} - Update an existing course")
    void updateCourse() {
        // create course
        Course course = new Course();
        course.setTitle("Test Course");
        course.setDescription("Description");
        course = courseRepository.save(course);

        Course updatedDetails = new Course();
        updatedDetails.setTitle("New Test Course");
        updatedDetails.setDescription("New Description");

        // put in new details for the course to be updated
        HttpEntity<Course> requestEntity = new HttpEntity<>(updatedDetails);

        // send request
        ResponseEntity<Course> response = restTemplate.exchange(
                "/api/courses/" + course.getId(),
                HttpMethod.PUT,
                requestEntity,
                Course.class
        );

        // assert that course has been updated
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("New Test Course", response.getBody().getTitle());
        assertEquals("New Description", response.getBody().getDescription());
    }

    @Test
    @DisplayName("DELETE /api/courses/{id} - Should delete a course")
    void deleteCourse() {
        // create course
        Course course = courseRepository.save(new Course());

        // send delete request
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/courses/" + course.getId(),
                HttpMethod.DELETE,
                null,
                Void.class
        );

        // should return no content and there should be no id in the database
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertFalse(courseRepository.existsById(course.getId()));
    }

    @Test
    @DisplayName("DELETE /api/courses/{id} - Should return 409 CONFLICT if registered")
    void deleteCourse_Registered() {
        // create a student and a course
        Student student = studentRepository.save(new Student());
        Course course = courseRepository.save(new Course());

        // set the student and course tied to the registration
        Registration registration = new Registration();
        registration.setStudent(student);
        registration.setCourse(course);
        registrationRepository.save(registration);

        // try to delete the student while they are registered for a course
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/courses/" + course.getId(),
                HttpMethod.DELETE,
                null,
                Void.class
        );

        // the response should be 409 conflict
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(courseRepository.existsById(course.getId()));
    }
}