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

    private TestRestTemplate authenticationTemplate;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

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

        ResponseEntity<RegistrationResponseDTO> response = authenticationTemplate.postForEntity("/api/registrations", requestDto, RegistrationResponseDTO.class);

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

        ResponseEntity<RegistrationResponseDTO> createdResponse = authenticationTemplate.postForEntity("/api/registrations", createDto, RegistrationResponseDTO.class);
        Integer registrationId = createdResponse.getBody().getRegistrationID();

        ResponseEntity<RegistrationResponseDTO> response = authenticationTemplate.getForEntity("/api/registrations/" + registrationId, RegistrationResponseDTO.class);

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
        authenticationTemplate.postForEntity("/api/registrations", DTO1, RegistrationResponseDTO.class);

        RegistrationResponseDTO DTO2 = new RegistrationResponseDTO();
        DTO2.setStudentId(student.getId());
        DTO2.setCourseId(course2.getId());
        authenticationTemplate.postForEntity("/api/registrations", DTO2, RegistrationResponseDTO.class);

        ResponseEntity<List<RegistrationResponseDTO>> response = authenticationTemplate.exchange(
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
    @DisplayName("DELETE /api/registrations/{id} - Delete a registration")
    void deleteRegistration_shouldReturnNoContent() {
        Student student = studentRepository.save(new Student());
        Course course = courseRepository.save(new Course());
        RegistrationResponseDTO createDto = new RegistrationResponseDTO();
        createDto.setStudentId(student.getId());
        createDto.setCourseId(course.getId());

        ResponseEntity<RegistrationResponseDTO> createdResponse = authenticationTemplate.postForEntity("/api/registrations", createDto, RegistrationResponseDTO.class);
        Integer registrationID = createdResponse.getBody().getRegistrationID();

        ResponseEntity<Void> response = authenticationTemplate.exchange(
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


        ResponseEntity<List<RegistrationResponseDTO>> response = authenticationTemplate.exchange(
                "/api/registrations/studentid/" + student1.getId(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<RegistrationResponseDTO> registrations = response.getBody();
        assertNotNull(registrations);
        assertEquals(0, registrations.size());
    }

    @Test
    @DisplayName("GET /api/registrations/studentid/{id} - No registration found by student id")
    void getAllRegistrationsByStudentId_shouldReturnNotFound() {

        //        int nonExistentStudentId = -1;

        ResponseEntity<Object> response = authenticationTemplate.getForEntity(
                "/api/registrations/studentid/-1",
                Object.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}