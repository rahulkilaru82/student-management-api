package com.example.studentmanagement.controller;

import com.example.studentmanagement.domain.Course;
import com.example.studentmanagement.domain.Enrollment;
import com.example.studentmanagement.domain.Student;
import com.example.studentmanagement.service.StudentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@CrossOrigin
public class StudentController {

    private static final Logger log = LoggerFactory.getLogger(StudentController.class);

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public List<Student> list() {
        log.info("GET /api/students");
        return studentService.list();
    }

    @GetMapping("/{id}")
    public Student get(@PathVariable("id") Long id) {
        log.info("GET /api/students/{}", id);
        return studentService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Student create(@RequestBody @Valid Student body) {
        log.info("POST /api/students email={}", body.getEmail());
        return studentService.create(body);
    }

    @PutMapping("/{id}")
    public Student update(@PathVariable("id") Long id, @RequestBody @Valid Student body) {
        log.info("PUT /api/students/{} email={}", id, body.getEmail());
        return studentService.update(id, body);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        log.info("DELETE /api/students/{}", id);
        studentService.delete(id);
    }

    @GetMapping("/{id}/courses")
    public List<Course> listCourses(@PathVariable("id") Long id) {
        log.info("GET /api/students/{}/courses", id);
        return studentService.listCourses(id);
    }

    @PostMapping("/{id}/courses")
    @ResponseStatus(HttpStatus.CREATED)
    public Enrollment enroll(@PathVariable("id") Long id, @RequestParam("courseId") Long courseId) {
        log.info("POST /api/students/{}/courses?courseId={}", id, courseId);
        return studentService.enroll(id, courseId);
    }

    @DeleteMapping("/{id}/courses/{courseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unenroll(@PathVariable("id") Long id, @PathVariable("courseId") Long courseId) {
        log.info("DELETE /api/students/{}/courses/{}", id, courseId);
        studentService.unenroll(id, courseId);
    }
}
