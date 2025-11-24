package com.example.studentmanagement.controller;

import com.example.studentmanagement.domain.Course;
import com.example.studentmanagement.domain.Student;
import com.example.studentmanagement.service.StudentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isEmptyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudentController.class)
class TestStudentController {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private StudentService studentService;


    private Student student(long id) {
        Student s = new Student();
        s.setId(id);
        s.setFirstName("Alice");
        s.setLastName("Smith");
        s.setEmail("alice@example.com");
        s.setBirthDate(LocalDate.of(1990, 5, 10));
        return s;
    }

    private Course course(long id) {
        Course c = new Course();
        c.setId(id);
        c.setCode("CS" + id);
        c.setName("Course " + id);
        return c;
    }

    @Test
    void list_ok() throws Exception {
        when(studentService.list()).thenReturn(List.of(student(1), student(2)));

        mvc.perform(get("/api/students").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(studentService, times(1)).list();
    }

    @Test
    void get_ok() throws Exception {
        when(studentService.get(1L)).thenReturn(student(1));

        mvc.perform(get("/api/students/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("alice@example.com"));

        verify(studentService).get(1L);
    }

    @Test
    @DisplayName("Create Student")
    void create_student() throws Exception {
        Student body = student(0);
        Student saved = student(10);
        when(studentService.create(any(Student.class))).thenReturn(saved);

        mvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.firstName").value("Alice"));

        ArgumentCaptor<Student> captor = ArgumentCaptor.forClass(Student.class);
        verify(studentService).create(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void update_ok() throws Exception {
        Student body = student(0);
        body.setFirstName("Alicia");
        Student updated = student(1);
        updated.setFirstName("Alicia");
        when(studentService.update(eq(1L), any(Student.class))).thenReturn(updated);

        mvc.perform(put("/api/students/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Alicia"));

        verify(studentService).update(eq(1L), any(Student.class));
    }

    @Test
    void delete_noContent() throws Exception {
        doNothing().when(studentService).delete(5L);

        mvc.perform(delete("/api/students/{id}", 5))
                .andExpect(status().isNoContent())
                .andExpect(content().string(isEmptyString()));

        verify(studentService).delete(5L);
    }

    @Test
    void listCourses_ok() throws Exception {
        when(studentService.listCourses(2L)).thenReturn(List.of(course(100), course(101)));

        mvc.perform(get("/api/students/{id}/courses", 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].code").value("CS100"))
                .andExpect(jsonPath("$[1].name").value("Course 101"));

        verify(studentService).listCourses(2L);
    }


    @Test
    void unenroll_noContent() throws Exception {
        doNothing().when(studentService).unenroll(7L, 8L);

        mvc.perform(delete("/api/students/{id}/courses/{courseId}", 7, 8))
                .andExpect(status().isNoContent());

        verify(studentService).unenroll(7L, 8L);
    }
}
