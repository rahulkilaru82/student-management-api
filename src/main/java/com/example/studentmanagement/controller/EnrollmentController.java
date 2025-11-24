package com.example.studentmanagement.controller;

import com.example.studentmanagement.domain.Enrollment;
import com.example.studentmanagement.dto.EnrollmentDto;
import com.example.studentmanagement.dto.StudentCourseDto;
import com.example.studentmanagement.service.EnrollmentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@CrossOrigin(origins = "http://localhost:4200")
public class EnrollmentController {

    private final EnrollmentService service;

    public EnrollmentController(EnrollmentService service) {
        this.service = service;
    }

    @GetMapping("/student/{studentId}")
    public List<StudentCourseDto> byStudent(@PathVariable Long studentId) {
        return service.listByStudentId(studentId).stream()
                .map(e -> new StudentCourseDto(
                        e.getCourse().getId(),
                        e.getCourse().getCode(),
                        e.getCourse().getName(),
                        e.getGrade()
                ))
                .toList();
    }

    @PostMapping
    public EnrollmentDto enroll(@RequestBody EnrollmentDto body) {
        Enrollment e = service.enroll(body.studentId(), body.courseId());
        return new EnrollmentDto(
                e.getId().getStudentId(),
                e.getId().getCourseId(),
                e.getGrade()
        );
    }

    @PatchMapping("/grade")
    public EnrollmentDto setGrade(@RequestBody EnrollmentDto body) {
        var e = service.setGrade(body.studentId(), body.courseId(), body.grade());
        return new EnrollmentDto(e.getId().getStudentId(), e.getId().getCourseId(), e.getGrade());
    }

    @DeleteMapping
    public void unenroll(@RequestParam Long studentId, @RequestParam Long courseId) {
        service.unenroll(studentId, courseId);
    }
}
