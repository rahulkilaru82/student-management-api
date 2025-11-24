package com.example.studentmanagement.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "enrollments")
public class Enrollment {

    @EmbeddedId
    private EnrollmentId id = new EnrollmentId();

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @MapsId("studentId")
    @JoinColumn(name = "student_id")
    @JsonIgnore
    private Student student;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @MapsId("courseId")
    @JoinColumn(name = "course_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Course course;

    @Column(name = "grade")
    private String grade;

    public EnrollmentId getId() {
        return id;
    }

    public void setId(EnrollmentId id) {
        this.id = id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }
}
