package com.example.registration.dto;

import com.example.registration.model.Registration;


public class RegistrationResponseDTO {

    private Long registrationID;
    private Long studentId;
    private String studentName;
    private Long courseId;
    private String courseTitle;


    public RegistrationResponseDTO() {
    }

    public RegistrationResponseDTO(Registration registration) {
        this.registrationID = registration.getRegistrationID();
        this.studentId = registration.getStudent().getId();
        this.studentName = registration.getStudent().getName();
        this.courseId = registration.getCourse().getId();
        this.courseTitle = registration.getCourse().getTitle();
    }




    public Long getRegistrationID() {
        return registrationID;
    }

    public Long getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public Long getCourseId() {
        return courseId;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setRegistrationID(Long registrationID) {
        this.registrationID = registrationID;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }
}