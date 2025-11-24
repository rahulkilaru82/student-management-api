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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestStudentService {

    @Mock
    private StudentRepository students;
    @Mock
    private CourseRepository courses;
    @Mock
    private EnrollmentRepository enrollments;

    @InjectMocks
    private StudentService service;

    private Student student(long id, String email) {
        Student s = new Student();
        s.setId(id);
        s.setFirstName("First");
        s.setLastName("Last");
        s.setEmail(email);
        s.setBirthDate(LocalDate.of(1990, 1, 1));
        return s;
    }

    private Course course(long id, String code, String name) {
        Course c = new Course();
        c.setId(id);
        c.setCode(code);
        c.setName(name);
        return c;
    }

    private Enrollment enrollment(long sid, long cid) {
        Enrollment e = new Enrollment();
        e.setId(new EnrollmentId(sid, cid));
        e.setStudent(student(sid, "s" + sid + "@ex.com"));
        e.setCourse(course(cid, "CS" + cid, "N" + cid));
        return e;
    }

    @BeforeEach
    void resetAll() {
        Mockito.reset(students, courses, enrollments);
    }

    @Test
    void list_ok() {
        when(students.findAll()).thenReturn(List.of(student(1, "a@x.com"), student(2, "b@x.com")));
        var out = service.list();
        assertThat(out).hasSize(2);
        verify(students).findAll();
    }

    @Test
    void get_ok() {
        when(students.findById(5L)).thenReturn(Optional.of(student(5, "a@x.com")));
        var s = service.get(5L);
        assertThat(s.getId()).isEqualTo(5L);
        verify(students).findById(5L);
    }

    @Test
    void get_notFound() {
        when(students.findById(9L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.get(9L));
        verify(students).findById(9L);
    }

    @Test
    void create_duplicateEmail() {
        var body = student(0, "dup@x.com");
        when(students.findByEmail("dup@x.com")).thenReturn(Optional.of(student(7, "dup@x.com")));
        assertThrows(BadRequestException.class, () -> service.create(body));
        verify(students).findByEmail("dup@x.com");
        verify(students, never()).save(any());
    }

    @Test
    void update_ok_sameEmail() {
        var existing = student(3, "same@x.com");
        when(students.findById(3L)).thenReturn(Optional.of(existing));

        var body = student(0, "same@x.com");
        body.setFirstName("Changed");
        body.setLastName("Name");
        body.setBirthDate(LocalDate.of(1991, 2, 2));

        var out = service.update(3L, body);

        assertThat(out.getId()).isEqualTo(3L);
        assertThat(out.getFirstName()).isEqualTo("Changed");
        assertThat(out.getEmail()).isEqualTo("same@x.com");
        verify(students).findById(3L);
        verify(students, never()).findByEmail(anyString());
        verify(students, never()).save(any());
    }

    @Test
    void update_ok_changeEmail_unique() {
        var existing = student(4, "old@x.com");
        when(students.findById(4L)).thenReturn(Optional.of(existing));
        when(students.findByEmail("new@x.com")).thenReturn(Optional.empty());

        var body = student(0, "new@x.com");
        body.setFirstName("A");
        body.setLastName("B");

        var out = service.update(4L, body);

        assertThat(out.getEmail()).isEqualTo("new@x.com");
        assertThat(out.getFirstName()).isEqualTo("A");
        verify(students).findById(4L);
        verify(students).findByEmail("new@x.com");
        verify(students, never()).save(any());
    }

    @Test
    void update_changeEmail_duplicate() {
        var existing = student(4, "old@x.com");
        when(students.findById(4L)).thenReturn(Optional.of(existing));
        when(students.findByEmail("dup@x.com")).thenReturn(Optional.of(student(8, "dup@x.com")));

        var body = student(0, "dup@x.com");
        assertThrows(BadRequestException.class, () -> service.update(4L, body));

        verify(students).findById(4L);
        verify(students).findByEmail("dup@x.com");
    }

    @Test
    void delete_ok() {
        var existing = student(6, "d@x.com");
        when(students.findById(6L)).thenReturn(Optional.of(existing));

        service.delete(6L);

        verify(students).findById(6L);
        verify(students).delete(existing);
    }

    @Test
    void enroll_ok() {
        var s = student(2, "s@x.com");
        var c = course(3, "CS003", "Algo");
        when(students.findById(2L)).thenReturn(Optional.of(s));
        when(courses.findById(3L)).thenReturn(Optional.of(c));
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
    void enroll_courseNotFound() {
        when(students.findById(2L)).thenReturn(Optional.of(student(2, "s@x.com")));
        when(courses.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.enroll(2L, 99L));
        verify(students).findById(2L);
        verify(courses).findById(99L);
        verify(enrollments, never()).save(any());
    }

    @Test
    void enroll_duplicate() {
        when(students.findById(2L)).thenReturn(Optional.of(student(2, "s@x.com")));
        when(courses.findById(3L)).thenReturn(Optional.of(course(3, "CS003", "Algo")));
        when(enrollments.existsById(any(EnrollmentId.class))).thenReturn(true);
        assertThrows(BadRequestException.class, () -> service.enroll(2L, 3L));
        verify(enrollments, never()).save(any());
    }

    @Test
    void unenroll_ok() {
        var existing = enrollment(7, 8);
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

    @Test
    void listCourses_ok() {
        var list = List.of(course(100, "CS100", "Intro"), course(101, "CS101", "DSA"));
        when(enrollments.findCoursesByStudentId(5L)).thenReturn(list);

        var out = service.listCourses(5L);

        assertThat(out).hasSize(2);
        assertThat(out.get(0).getCode()).isEqualTo("CS100");
        verify(enrollments).findCoursesByStudentId(5L);
    }
}
