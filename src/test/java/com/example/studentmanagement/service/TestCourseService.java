package com.example.studentmanagement.service;

import com.example.studentmanagement.domain.Course;
import com.example.studentmanagement.exception.BadRequestException;
import com.example.studentmanagement.exception.NotFoundException;
import com.example.studentmanagement.repo.CourseRepository;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestCourseService {

    @Mock
    private CourseRepository courseRepository;
    @InjectMocks
    private CourseService service;

    private Course course(long id, String code, String name) {
        Course c = new Course();
        c.setId(id);
        c.setCode(code);
        c.setName(name);
        return c;
    }

    @BeforeEach
    void init() {
        Mockito.reset(courseRepository);
    }

    @Test
    void list_ok() {
        when(courseRepository.findAll()).thenReturn(List.of(course(1, "CS101", "Intro")));
        var out = service.list();
        assertThat(out).hasSize(1);
        assertThat(out.get(0).getCode()).isEqualTo("CS101");
        verify(courseRepository).findAll();
    }

    @Test
    void get_ok() {
        when(courseRepository.findById(5L)).thenReturn(Optional.of(course(5, "CS105", "Sys")));
        var out = service.get(5L);
        assertThat(out.getId()).isEqualTo(5L);
        verify(courseRepository).findById(5L);
    }

    @Test
    void get_notFound() {
        when(courseRepository.findById(9L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.get(9L));
        verify(courseRepository).findById(9L);
    }

    @Test
    void create_ok() {
        var body = course(0, "CS200", "Algo");
        when(courseRepository.findByCode("CS200")).thenReturn(Optional.empty());
        when(courseRepository.save(any(Course.class))).thenAnswer(inv -> {
            Course c = inv.getArgument(0);
            c.setId(11L);
            return c;
        });

        var out = service.create(body);

        assertThat(out.getId()).isEqualTo(11L);
        assertThat(out.getCode()).isEqualTo("CS200");
        assertThat(out.getName()).isEqualTo("Algo");
        verify(courseRepository).findByCode("CS200");
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    void create_duplicateCode() {
        var body = course(0, "CS200", "Algo");
        when(courseRepository.findByCode("CS200")).thenReturn(Optional.of(course(99, "CS200", "X")));
        assertThrows(BadRequestException.class, () -> service.create(body));
        verify(courseRepository).findByCode("CS200");
        verify(courseRepository, never()).save(any());
    }

    @Test
    void update_ok_sameCode() {
        var existing = course(7, "CS300", "Old");
        when(courseRepository.findById(7L)).thenReturn(Optional.of(existing));

        var body = course(0, "CS300", "New Name");
        var out = service.update(7L, body);

        assertThat(out.getId()).isEqualTo(7L);
        assertThat(out.getCode()).isEqualTo("CS300");
        assertThat(out.getName()).isEqualTo("New Name");
        verify(courseRepository).findById(7L);
        verify(courseRepository, never()).findByCode(anyString());
        verify(courseRepository, never()).save(any());
    }

    @Test
    void update_ok_changeCode_unique() {
        var existing = course(7, "CS300", "Old");
        when(courseRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(courseRepository.findByCode("CS301")).thenReturn(Optional.empty());

        var body = course(0, "CS301", "Renamed");
        var out = service.update(7L, body);

        assertThat(out.getCode()).isEqualTo("CS301");
        assertThat(out.getName()).isEqualTo("Renamed");
        verify(courseRepository).findById(7L);
        verify(courseRepository).findByCode("CS301");
        verify(courseRepository, never()).save(any());
    }

    @Test
    void update_changeCode_duplicate() {
        var existing = course(7, "CS300", "Old");
        when(courseRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(courseRepository.findByCode("CS300X")).thenReturn(Optional.of(course(8, "CS300X", "Other")));

        var body = course(0, "CS300X", "Name");
        assertThrows(BadRequestException.class, () -> service.update(7L, body));
        verify(courseRepository).findById(7L);
        verify(courseRepository).findByCode("CS300X");
    }

    @Test
    void delete_ok() {
        var existing = course(12, "CS400", "Compilers");
        when(courseRepository.findById(12L)).thenReturn(Optional.of(existing));

        service.delete(12L);

        verify(courseRepository).findById(12L);
        verify(courseRepository).delete(existing);
    }
}
