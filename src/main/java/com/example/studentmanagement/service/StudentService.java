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
public class StudentService {

    private static final Logger log = LoggerFactory.getLogger(StudentService.class);

    private final StudentRepository students;
    private final CourseRepository courses;
    private final EnrollmentRepository enrollments;

    public StudentService(StudentRepository students,
                          CourseRepository courses,
                          EnrollmentRepository enrollments) {
        this.students = students;
        this.courses = courses;
        this.enrollments = enrollments;
    }

    public List<Student> list() {
        log.debug("Listing students");
        return students.findAll();
    }

    public Student get(Long id) {
        return students.findById(id)
                .orElseThrow(() -> new NotFoundException("Student %d not found".formatted(id)));
    }

    @Transactional
    public Student create(Student body) {
        log.info("Create student email={}", body.getEmail());
        students.findByEmail(body.getEmail()).ifPresent(x -> {
            throw new BadRequestException("Email already exists");
        });
        return students.save(body);
    }

    @Transactional
    public Student update(Long id, Student body) {
        log.info("Update student id={} email={}", id, body.getEmail());
        Student s = get(id);
        if (!s.getEmail().equals(body.getEmail())) {
            students.findByEmail(body.getEmail()).ifPresent(x -> {
                throw new BadRequestException("Email already exists");
            });
        }
        s.setFirstName(body.getFirstName());
        s.setLastName(body.getLastName());
        s.setEmail(body.getEmail());
        s.setBirthDate(body.getBirthDate());
        return s;
    }

    @Transactional
    public void delete(Long id) {
        log.info("Delete student id={}", id);
        students.delete(get(id));
    }

    @Transactional
    public Enrollment enroll(Long studentId, Long courseId) {
        log.info("Enroll studentId={} courseId={}", studentId, courseId);
        Student s = get(studentId);
        Course c = courses.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course %d not found".formatted(courseId)));
        EnrollmentId eid = new EnrollmentId(s.getId(), c.getId());
        if (enrollments.existsById(eid)) {
            throw new BadRequestException("Student already enrolled in course");
        }
        Enrollment e = new Enrollment();
        e.setId(eid);
        e.setStudent(s);
        e.setCourse(c);
        return enrollments.save(e);
    }

    @Transactional
    public void unenroll(Long studentId, Long courseId) {
        log.info("Unenroll studentId={} courseId={}", studentId, courseId);
        EnrollmentId eid = new EnrollmentId(studentId, courseId);
        Enrollment e = enrollments.findById(eid)
                .orElseThrow(() -> new NotFoundException("Enrollment not found"));
        enrollments.delete(e);
    }

    public List<Course> listCourses(Long studentId) {
        log.debug("List courses by studentId={}", studentId);
        return enrollments.findCoursesByStudentId(studentId);
    }
}
