package com.example.registration;

import com.example.registration.dto.RegistrationResponseDTO;
import com.example.registration.model.Course;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestRegistration {

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

    @Test
    @DisplayName("POST /api/registrations - Create a new registration")
    void createRegistration() {
        // create a student and a course
        Student student = new Student();
        student.setName("Student1");
        student = studentRepository.save(student);

        Course course = new Course();
        course.setTitle("Course1");
        course = courseRepository.save(course);

        // construct DTO for the registration
        RegistrationResponseDTO requestDto = new RegistrationResponseDTO();
        requestDto.setStudentId(student.getId());
        requestDto.setCourseId(course.getId());

        // create response
        ResponseEntity<RegistrationResponseDTO> response = restTemplate.postForEntity("/api/registrations", requestDto, RegistrationResponseDTO.class);

        // the response should be 201 with the correct DTO data
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(student.getId(), response.getBody().getStudentId());
        assertEquals(course.getId(), response.getBody().getCourseId());

        // compare the student/course with what it says in the registration
        assertEquals("Student1", response.getBody().getStudentName());
        assertEquals("Course1", response.getBody().getCourseTitle());
    }

    @Test
    @DisplayName("GET /api/registrations/{id} - Return a registration DTO")
    void getRegistration() {
        // creating student and course
        Student student = studentRepository.save(new Student());
        Course course = courseRepository.save(new Course());

        // create DTO
        RegistrationResponseDTO createDto = new RegistrationResponseDTO();
        createDto.setStudentId(student.getId());
        createDto.setCourseId(course.getId());

        // create POST request
        ResponseEntity<RegistrationResponseDTO> createdResponse = restTemplate.postForEntity("/api/registrations", createDto, RegistrationResponseDTO.class);

        // grab the ID from the response
        Integer registrationId = createdResponse.getBody().getRegistrationID();

        // create GET request based on the registrationID
        ResponseEntity<RegistrationResponseDTO> response = restTemplate.getForEntity("/api/registrations/" + registrationId, RegistrationResponseDTO.class);

        // comparing the results
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(registrationId, response.getBody().getRegistrationID());
    }

    @Test
    @DisplayName("GET /api/registrations - Return all registrations")
    void getAllRegistrations_shouldReturnListOfDTOs() {
        // a student with two courses
        Student student = studentRepository.save(new Student());
        Course course1 = courseRepository.save(new Course());
        Course course2 = courseRepository.save(new Course());

        // create two registrations with the DTOs
        RegistrationResponseDTO DTO1 = new RegistrationResponseDTO();
        DTO1.setStudentId(student.getId());
        DTO1.setCourseId(course1.getId());
        restTemplate.postForEntity("/api/registrations", DTO1, RegistrationResponseDTO.class);

        RegistrationResponseDTO DTO2 = new RegistrationResponseDTO();
        DTO2.setStudentId(student.getId());
        DTO2.setCourseId(course2.getId());
        restTemplate.postForEntity("/api/registrations", DTO2, RegistrationResponseDTO.class);

        // GET request for all registrations
        ResponseEntity<List<RegistrationResponseDTO>> response = restTemplate.exchange(
                "/api/registrations",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // assert the response is good and we have 2 registrations returned
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    @Test
    @DisplayName("DELETE /api/registrations/{id} - Should delete a registration")
    void deleteRegistration_shouldReturnNoContent() {
        // create a registration with the DTO
        Student student = studentRepository.save(new Student());
        Course course = courseRepository.save(new Course());
        RegistrationResponseDTO createDto = new RegistrationResponseDTO();
        createDto.setStudentId(student.getId());
        createDto.setCourseId(course.getId());

        // send response to create DTO
        ResponseEntity<RegistrationResponseDTO> createdResponse = restTemplate.postForEntity("/api/registrations", createDto, RegistrationResponseDTO.class);

        // grab the ID of the registration
        Integer registrationID = createdResponse.getBody().getRegistrationID();

        // DELETE request
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/registrations/" + registrationID,
                HttpMethod.DELETE,
                null,
                Void.class
        );

        // should return 204 no content
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertFalse(registrationRepository.existsById(registrationID));
    }
}