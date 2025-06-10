package com.example.registration.service;

import com.example.registration.model.Course;
import com.example.registration.repository.CourseRepository;
import com.example.registration.repository.RegistrationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CourseService {

    private final CourseRepository courseRepo;
    private final RegistrationRepository registrationRepo;

    public CourseService(CourseRepository courseRepo, RegistrationRepository registrationRepo) {
        this.courseRepo = courseRepo;
        this.registrationRepo = registrationRepo;
    }

    // Finding every single course
    public List<Course> getAllCourses() {
        return courseRepo.findAll();
    }

    // Finding course by id
    public Course getCourseById(Integer id) {
        return courseRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found with id: " + id));
    }

    // Create a course
    public Course createCourse(Course course) {
        return courseRepo.save(course);
    }

    // Update a course, changing the title and description both in Course and Registration tables
    public Course updateCourse(Integer id, Course updatedCourseDetails) {
        Course course = getCourseById(id);
        course.setTitle(updatedCourseDetails.getTitle());
        course.setDescription(updatedCourseDetails.getDescription());
        return courseRepo.save(course);
    }

    // Delete a course by id
    public void deleteCourse(Integer id) {

        // course must exist
        if (!courseRepo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found with id: " + id);
        }
        // course must not be tied to a registration
        if (registrationRepo.existsByCourseId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot delete course with existing registrations.");
        }
        courseRepo.deleteById(id);
    }
}