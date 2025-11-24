package com.example.studentmanagement.controller;

import com.example.studentmanagement.domain.Course;
import com.example.studentmanagement.domain.Enrollment;
import com.example.studentmanagement.dto.EnrollmentDto;
import com.example.studentmanagement.service.EnrollmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EnrollmentController.class)
class TestEnrollmentController {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private EnrollmentService service;

    private Course course(long id, String code, String name) {
        Course c = new Course();
        c.setId(id);
        c.setCode(code);
        c.setName(name);
        return c;
    }

    private Enrollment enrollmentMock(long studentId, long courseId, Course course, String grade) {
        Enrollment e = Mockito.mock(Enrollment.class);
        Object idObj = Mockito.mock(Object.class, Mockito.withSettings().name("EnrollmentId"));
        try {
            Mockito.when(idObj.getClass().getMethod("getStudentId").invoke(idObj)).thenReturn(studentId);
            Mockito.when(idObj.getClass().getMethod("getCourseId").invoke(idObj)).thenReturn(courseId);
        } catch (Throwable ignore) {
        }
        Enrollment eDeep = Mockito.mock(Enrollment.class, Mockito.RETURNS_DEEP_STUBS);
        when(eDeep.getId().getStudentId()).thenReturn(studentId);
        when(eDeep.getId().getCourseId()).thenReturn(courseId);
        when(eDeep.getCourse()).thenReturn(course);
        when(eDeep.getGrade()).thenReturn(grade);
        return eDeep;
    }


    @Test
    void enroll_ok() throws Exception {
        EnrollmentDto req = new EnrollmentDto(2L, 3L, null);

        Course c = course(3L, "CS003", "Algorithms");
        Enrollment e = enrollmentMock(2L, 3L, c, null);
        when(service.enroll(2L, 3L)).thenReturn(e);

        mvc.perform(post("/api/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value(2))
                .andExpect(jsonPath("$.courseId").value(3))
                .andExpect(jsonPath("$.grade").doesNotExist());
    }

    @Test
    void setGrade_ok() throws Exception {
        EnrollmentDto req = new EnrollmentDto(2L, 3L, "A");
        Course c = course(3L, "CS003", "Algorithms");
        Enrollment e = enrollmentMock(2L, 3L, c, "A");
        when(service.setGrade(2L, 3L, "A")).thenReturn(e);

        mvc.perform(patch("/api/enrollments/grade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value(2))
                .andExpect(jsonPath("$.courseId").value(3))
                .andExpect(jsonPath("$.grade").value("A"));
    }

}
