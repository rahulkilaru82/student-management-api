package com.example.studentmanagement.controller;

import com.example.studentmanagement.domain.Course;
import com.example.studentmanagement.service.CourseService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@CrossOrigin
public class CourseController {

    private static final Logger log = LoggerFactory.getLogger(CourseController.class);

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public List<Course> list() {
        log.info("GET /api/courses");
        return courseService.list();
    }

    @GetMapping("/{id}")
    public Course get(@PathVariable("id") Long id) {
        log.info("GET /api/courses/{}", id);
        return courseService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Course create(@RequestBody @Valid Course body) {
        log.info("POST /api/courses code={}", body.getCode());
        return courseService.create(body);
    }

    @PutMapping("/{id}")
    public Course update(@PathVariable("id") Long id, @RequestBody @Valid Course body) {
        log.info("PUT /api/courses/{} code={}", id, body.getCode());
        return courseService.update(id, body);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        log.info("DELETE /api/courses/{}", id);
        courseService.delete(id);
    }
}
