package com.example.registration.model;

import jakarta.persistence.*;

@Entity
@Table(name = "registrations")
public class Registration {

    // table attributes
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "registration_id")
    private Integer registration_id;

    // taken from student table
    @Column(name = "student_name")
    private String studentName;

    // taken from student table
    @Column(name = "student_email")
    private String studentEmail;

    // taken from student table
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", referencedColumnName = "student_id")
    private Student student;

    // taken from course table
    @Column(name = "course_name")
    private String courseName;

    // taken from course table
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", referencedColumnName = "course_id")
    private Course course;

    // getters
    public Integer getRegistrationID() {
        return registration_id;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public Student getStudent() {
        return student;
    }

    public String getCourseName() {
        return courseName;
    }

    public Course getCourse() {
        return course;
    }

    // setters
    public void setRegistrationID(Integer registrationID) {
        this.registration_id = registrationID;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public void setCourse(Course course) {
        this.course = course;
    }
}