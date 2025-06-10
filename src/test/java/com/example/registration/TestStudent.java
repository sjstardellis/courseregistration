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

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private RegistrationRepository registrationRepository;


    // removes all data before and after testing
    @AfterEach
    public void deleteAllData() {
        registrationRepository.deleteAll();
        studentRepository.deleteAll();
        courseRepository.deleteAll();
    }


    // creating a student test and checking the response
    @Test
    @DisplayName("POST /api/students - Create a new student")
    public void createStudent() {
        // create new student object
        Student newStudent = new Student();

        newStudent.setName("Test Student");
        newStudent.setEmail("test@example.com");

        // a post request is made to create the student
        ResponseEntity<Student> response = restTemplate.postForEntity("/api/students", newStudent, Student.class);

        // the response created should be with the student's data
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals("Test Student", response.getBody().getName());
        assertEquals("test@example.com", response.getBody().getEmail());
    }

    private static Stream<Arguments> createMultipleStudents() {
        return Stream.of(
                Arguments.of("Jane Smith", "jane.smith@example.com"),
                Arguments.of("Peter Jones", "peter.jones@example.com"),
                Arguments.of("Mary Brown", "mary.brown@example.com")
        );
    }

    @ParameterizedTest
    @MethodSource("createMultipleStudents")
    @DisplayName("POST /api/students - Create multiple new students")
    public void createMultipleStudents(String name, String email) {

        // create a new student based on the arguments from the stream
        Student newStudent = new Student();
        newStudent.setName(name);
        newStudent.setEmail(email);

        // a post request is made to create the student
        ResponseEntity<Student> response = restTemplate.postForEntity("/api/students", newStudent, Student.class);

        // assertEquals each of the students created from the stream
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(name, response.getBody().getName());
        assertTrue(studentRepository.existsById(response.getBody().getId()));
    }

    @Test
    @DisplayName("GET /api/students/{id} - Return a student")
    void getStudentByID() {

        // have an existing student in the database
        Student student = new Student();
        student.setName("Test Student");
        student.setEmail("test@example.com");
        student = studentRepository.save(student);

        // get a student
        ResponseEntity<Student> response = restTemplate.getForEntity("/api/students/" + student.getId(), Student.class);

        // should return ok with the correct student
        assertEquals(HttpStatus.OK, response.getStatusCode());
        // not null check
        assertNotNull(response.getBody());
        // comparing the IDs of the student in the database and the student in the response
        assertEquals(student.getId(), response.getBody().getId());
    }

    @Test
    @DisplayName("GET /api/students/{id} - Return 404")
    void getStudentByInvalidID() {
        // get a student based on a non-existent ID
        ResponseEntity<Student> response = restTemplate.getForEntity("/api/students/-1", Student.class);

        // should return 404
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("GET /api/students - Return all students")
    void getAllStudents() {
        // save multiple students to the database
        Student student1 = new Student();
        student1.setName("John");
        student1.setEmail("john@example.com");
        studentRepository.save(student1);

        Student student2 = new Student();
        student2.setName("Ben");
        student2.setEmail("ben@example.com");
        studentRepository.save(student2);

        // fetch all students
        ResponseEntity<List<Student>> response = restTemplate.exchange(
                "/api/students",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // should retrieve a list of all students of size 2, not null
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    @Test
    @DisplayName("PUT /api/students/{id} - Update an existing student")
    void updateStudentByID() {
        // create a student
        Student student = new Student();
        student.setName("Test1");
        student.setEmail("test1@example.com");
        student = studentRepository.save(student);

        // update the student details with
        Student updatedDetails = new Student();
        updatedDetails.setName("Test2");
        updatedDetails.setEmail("test2@example.com");

        // put request initiated with updatedDetails of the student
        HttpEntity<Student> requestEntity = new HttpEntity<>(updatedDetails);

        // completes request
        ResponseEntity<Student> response = restTemplate.exchange("/api/students/" + student.getId(),
                HttpMethod.PUT,
                requestEntity,
                Student.class
        );

        // ensure the response is updated properly
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test2", response.getBody().getName());
        assertEquals("test2@example.com", response.getBody().getEmail());
    }

    @Test
    @DisplayName("DELETE /api/students/{id} - Delete a student")
    public void deleteStudent() {
        // create a new student
        Student student = new Student();
        student.setName("Deleted Student");
        student.setEmail("delete@example.com");

        // save student
        student = studentRepository.save(student);

        // delete request for the student that we just created
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/students/" + student.getId(),
                HttpMethod.DELETE,
                null,
                Void.class
        );

        // should return 204 no content
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertFalse(studentRepository.existsById(student.getId()));
    }

    @Test
    @DisplayName("DELETE /api/students/{id} - Return 409 if the user is registered for a course")
    void deleteStudent_Registered() {
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
                "/api/students/" + student.getId(),
                HttpMethod.DELETE,
                null,
                Void.class
        );

        // the response should be 409 conflict
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(studentRepository.existsById(student.getId()));
    }
}