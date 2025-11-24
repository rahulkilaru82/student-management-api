package com.example.studentmanagement.repo;

import com.example.studentmanagement.domain.Enrollment;
import com.example.studentmanagement.domain.EnrollmentId;
import com.example.studentmanagement.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, EnrollmentId> {

    @Query("select e.course from Enrollment e join e.course c where e.id.studentId = :studentId")
    List<Course> findCoursesByStudentId(@Param("studentId") Long studentId);
}
