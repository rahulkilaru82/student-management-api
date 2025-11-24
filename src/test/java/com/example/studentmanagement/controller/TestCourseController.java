package com.example.studentmanagement.controller;

import com.example.studentmanagement.domain.Course;
import com.example.studentmanagement.service.CourseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourseController.class)
class TestCourseController {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private CourseService courseService;

    private Course course(long id, String code, String name) {
        Course c = new Course();
        c.setId(id);
        c.setCode(code);
        c.setName(name);
        return c;
    }

    @Test
    void list_ok() throws Exception {
        when(courseService.list()).thenReturn(List.of(
                course(1, "CS101", "Intro"),
                course(2, "CS102", "DSA")
        ));

        mvc.perform(get("/api/courses").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].code").value("CS101"))
                .andExpect(jsonPath("$[0].name").value("Intro"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].code").value("CS102"))
                .andExpect(jsonPath("$[1].name").value("DSA"));
    }

    @Test
    void get_ok() throws Exception {
        when(courseService.get(3L)).thenReturn(course(3, "CS103", "Systems"));

        mvc.perform(get("/api/courses/{id}", 3))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.code").value("CS103"))
                .andExpect(jsonPath("$.name").value("Systems"));
    }

    @Test
    void create_created() throws Exception {
        Course req = course(0, "CS200", "Algorithms");
        Course saved = course(10, "CS200", "Algorithms");
        when(courseService.create(any(Course.class))).thenReturn(saved);

        mvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.code").value("CS200"))
                .andExpect(jsonPath("$.name").value("Algorithms"));
    }

    @Test
    void update_ok() throws Exception {
        Course req = course(0, "CS201", "Advanced Algorithms");
        Course upd = course(10, "CS201", "Advanced Algorithms");
        when(courseService.update(eq(10L), any(Course.class))).thenReturn(upd);

        mvc.perform(put("/api/courses/{id}", 10)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.code").value("CS201"))
                .andExpect(jsonPath("$.name").value("Advanced Algorithms"));
    }

    @Test
    void delete_noContent() throws Exception {
        doNothing().when(courseService).delete(7L);

        mvc.perform(delete("/api/courses/{id}", 7))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }
}
