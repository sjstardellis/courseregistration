package com.example.registration.model;

import jakarta.persistence.*;

@Entity
@Table(name = "registrations")
public class Registration {

    // table attributes
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "registrationID")
    private Long registrationID;

    // taken from student table
    @Column(name = "student_name")
    private String studentName;

    // taken from student table
    @Column(name = "student_email")
    private String studentEmail;

    // taken from student table
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", referencedColumnName = "id")
    private Student student;

    // taken from course table
    @Column(name = "course_name")
    private String courseName;

    // taken from course table
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", referencedColumnName = "id")
    private Course course;

    // getters
    public Long getRegistrationID() {
        return registrationID;
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
    public void setRegistrationID(Long registrationID) {
        this.registrationID = registrationID;
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