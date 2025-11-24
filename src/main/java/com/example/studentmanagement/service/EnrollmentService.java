package com.example.studentmanagement.service;

import com.example.studentmanagement.domain.Course;
import com.example.studentmanagement.domain.Enrollment;
import com.example.studentmanagement.domain.EnrollmentId;
import com.example.studentmanagement.domain.Student;
import com.example.studentmanagement.exception.BadRequestException;
import com.example.studentmanagement.exception.NotFoundException;
import com.example.studentmanagement.repo.CourseRepository;
import com.example.studentmanagement.repo.EnrollmentRepository;
import com.example.studentmanagement.repo.StudentRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EnrollmentService {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentService.class);

    private final EnrollmentRepository enrollments;
    private final StudentRepository students;
    private final CourseRepository courses;

    public EnrollmentService(EnrollmentRepository enrollments,
                             StudentRepository students,
                             CourseRepository courses) {
        this.enrollments = enrollments;
        this.students = students;
        this.courses = courses;
    }

    public List<Enrollment> list() {
        log.debug("Listing all enrollments");
        return enrollments.findAll();
    }

    public List<Enrollment> listByStudentId(Long studentId) {
        log.debug("Listing enrollments by studentId={}", studentId);
        return enrollments.findAll().stream()
                .filter(e -> e.getId().getStudentId().equals(studentId))
                .toList();
    }

    public List<Enrollment> listByCourseId(Long courseId) {
        log.debug("Listing enrollments by courseId={}", courseId);
        return enrollments.findAll().stream()
                .filter(e -> e.getId().getCourseId().equals(courseId))
                .toList();
    }

    @Transactional
    public Enrollment enroll(Long studentId, Long courseId) {
        log.info("Enroll studentId={} courseId={}", studentId, courseId);
        Student s = students.findById(studentId)
                .orElseThrow(() -> new NotFoundException("Student %d not found".formatted(studentId)));
        Course c = courses.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course %d not found".formatted(courseId)));

        EnrollmentId id = new EnrollmentId(s.getId(), c.getId());
        if (enrollments.existsById(id)) {
            throw new BadRequestException("Student already enrolled in course");
        }

        Enrollment e = new Enrollment();
        e.setId(id);
        e.setStudent(s);
        e.setCourse(c);
        return enrollments.save(e);
    }

    @Transactional
    public Enrollment setGrade(Long studentId, Long courseId, String grade) {
        log.info("Set grade studentId={} courseId={} grade={}", studentId, courseId, grade);
        EnrollmentId id = new EnrollmentId(studentId, courseId);
        Enrollment e = enrollments.findById(id)
                .orElseThrow(() -> new NotFoundException("Enrollment not found"));
        e.setGrade(grade);
        return e;
    }

    @Transactional
    public void unenroll(Long studentId, Long courseId) {
        log.info("Unenroll studentId={} courseId={}", studentId, courseId);
        EnrollmentId id = new EnrollmentId(studentId, courseId);
        Enrollment e = enrollments.findById(id)
                .orElseThrow(() -> new NotFoundException("Enrollment not found"));
        enrollments.delete(e);
    }

}
