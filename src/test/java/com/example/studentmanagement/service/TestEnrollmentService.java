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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestEnrollmentService {

    @Mock
    private EnrollmentRepository enrollments;
    @Mock
    private StudentRepository students;
    @Mock
    private CourseRepository courses;

    @InjectMocks
    private EnrollmentService service;

    private Student student(long id, String fn, String ln, String email) {
        Student s = new Student();
        s.setId(id);
        s.setFirstName(fn);
        s.setLastName(ln);
        s.setEmail(email);
        return s;
    }

    private Course course(long id, String code, String name) {
        Course c = new Course();
        c.setId(id);
        c.setCode(code);
        c.setName(name);
        return c;
    }

    private Enrollment enrollment(long studentId, long courseId, String grade) {
        Enrollment e = new Enrollment();
        e.setId(new EnrollmentId(studentId, courseId));
        e.setStudent(student(studentId, "A", "B", "a@b.com"));
        e.setCourse(course(courseId, "CS" + courseId, "C" + courseId));
        e.setGrade(grade);
        return e;
    }

    @BeforeEach
    void resetAll() {
        Mockito.reset(enrollments, students, courses);
    }

    @Test
    void list_ok() {
        when(enrollments.findAll()).thenReturn(List.of(enrollment(1, 100, "A"), enrollment(2, 101, "B")));
        var out = service.list();
        assertThat(out).hasSize(2);
        verify(enrollments).findAll();
    }

    @Test
    void listByStudentId_ok() {
        when(enrollments.findAll()).thenReturn(List.of(enrollment(5, 100, "A"), enrollment(6, 101, "B"), enrollment(5, 102, null)));
        var out = service.listByStudentId(5L);
        assertThat(out).hasSize(2);
        assertThat(out).allMatch(e -> e.getId().getStudentId().equals(5L));
        verify(enrollments).findAll();
    }

    @Test
    void listByCourseId_ok() {
        when(enrollments.findAll()).thenReturn(List.of(enrollment(5, 200, "A"), enrollment(6, 201, "B"), enrollment(7, 200, null)));
        var out = service.listByCourseId(200L);
        assertThat(out).hasSize(2);
        assertThat(out).allMatch(e -> e.getId().getCourseId().equals(200L));
        verify(enrollments).findAll();
    }

    @Test
    void enroll_ok() {
        when(students.findById(2L)).thenReturn(Optional.of(student(2, "A", "S", "a@b.com")));
        when(courses.findById(3L)).thenReturn(Optional.of(course(3, "CS003", "Algo")));
        when(enrollments.existsById(any(EnrollmentId.class))).thenReturn(false);
        when(enrollments.save(any(Enrollment.class))).thenAnswer(inv -> inv.getArgument(0, Enrollment.class));

        var e = service.enroll(2L, 3L);

        assertThat(e.getId().getStudentId()).isEqualTo(2L);
        assertThat(e.getId().getCourseId()).isEqualTo(3L);
        verify(students).findById(2L);
        verify(courses).findById(3L);
        verify(enrollments).existsById(any(EnrollmentId.class));
        verify(enrollments).save(any(Enrollment.class));
    }

    @Test
    void enroll_studentNotFound() {
        when(students.findById(9L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.enroll(9L, 3L));
        verify(students).findById(9L);
        verify(courses, never()).findById(anyLong());
        verify(enrollments, never()).save(any());
    }

    @Test
    void enroll_courseNotFound() {
        when(students.findById(2L)).thenReturn(Optional.of(student(2, "A", "S", "a@b.com")));
        when(courses.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.enroll(2L, 99L));
        verify(students).findById(2L);
        verify(courses).findById(99L);
        verify(enrollments, never()).save(any());
    }

    @Test
    void enroll_duplicate() {
        when(students.findById(2L)).thenReturn(Optional.of(student(2, "A", "S", "a@b.com")));
        when(courses.findById(3L)).thenReturn(Optional.of(course(3, "CS003", "Algo")));
        when(enrollments.existsById(any(EnrollmentId.class))).thenReturn(true);
        assertThrows(BadRequestException.class, () -> service.enroll(2L, 3L));
        verify(enrollments, never()).save(any());
    }

    @Test
    void setGrade_ok() {
        var existing = enrollment(2, 3, null);
        when(enrollments.findById(any(EnrollmentId.class))).thenReturn(Optional.of(existing));

        var out = service.setGrade(2L, 3L, "A");

        assertThat(out.getId().getStudentId()).isEqualTo(2L);
        assertThat(out.getId().getCourseId()).isEqualTo(3L);
        assertThat(out.getGrade()).isEqualTo("A");
        verify(enrollments).findById(any(EnrollmentId.class));
    }

    @Test
    void setGrade_notFound() {
        when(enrollments.findById(any(EnrollmentId.class))).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.setGrade(2L, 3L, "A"));
        verify(enrollments).findById(any(EnrollmentId.class));
    }

    @Test
    void unenroll_ok() {
        var existing = enrollment(7, 8, "B");
        when(enrollments.findById(any(EnrollmentId.class))).thenReturn(Optional.of(existing));

        service.unenroll(7L, 8L);

        verify(enrollments).findById(any(EnrollmentId.class));
        verify(enrollments).delete(existing);
    }

    @Test
    void unenroll_notFound() {
        when(enrollments.findById(any(EnrollmentId.class))).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.unenroll(7L, 8L));
        verify(enrollments).findById(any(EnrollmentId.class));
        verify(enrollments, never()).delete(any());
    }
}
