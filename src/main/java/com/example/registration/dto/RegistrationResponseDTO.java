package com.example.registration.dto;

import com.example.registration.model.Registration;


public class RegistrationResponseDTO {

    private Integer registrationID;
    private Integer studentId;
    private String studentName;
    private Integer courseId;
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




    public Integer getRegistrationID() {
        return registrationID;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public Integer getCourseId() {
        return courseId;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setRegistrationID(Integer registrationID) {
        this.registrationID = registrationID;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }
}