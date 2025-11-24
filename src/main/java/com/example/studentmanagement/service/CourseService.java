package com.example.studentmanagement.service;

import com.example.studentmanagement.domain.Course;
import com.example.studentmanagement.exception.BadRequestException;
import com.example.studentmanagement.exception.NotFoundException;
import com.example.studentmanagement.repo.CourseRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {
    private static final Logger log = LoggerFactory.getLogger(CourseService.class);
    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courses) {
        this.courseRepository = courses;
    }

    public List<Course> list() {
        log.debug("Listing all courses");
        return courseRepository.findAll();
    }

    public Course get(Long id) {
        log.debug("Fetching course id={}", id);
        return courseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Course %d not found".formatted(id)));
    }

    @Transactional
    public Course create(Course body) {
        log.info("Create course code={}", body.getCode());
        courseRepository.findByCode(body.getCode())
                .ifPresent(x -> {
                    throw new BadRequestException("Code already exists");
                });
        Course c = new Course();
        c.setCode(body.getCode());
        c.setName(body.getName());
        return courseRepository.save(c);
    }

    @Transactional
    public Course update(Long id, Course body) {
        log.info("Update course id={} code={}", id, body.getCode());
        Course c = get(id);
        if (!c.getCode().equals(body.getCode())) {
            courseRepository.findByCode(body.getCode())
                    .ifPresent(x -> {
                        throw new BadRequestException("Code already exists");
                    });
        }
        c.setCode(body.getCode());
        c.setName(body.getName());
        return c;
    }

    @Transactional
    public void delete(Long id) {
        log.info("Delete course id={}", id);
        courseRepository.delete(get(id));
    }
}
