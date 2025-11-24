Student Management API (Spring Boot, PostgreSQL)

REST service for students, courses, and enrollments.

Run

Prereqs: Java 17+, Maven, PostgreSQL running locally.

##DB Changes:

CREATE DATABASE studentdb;
CREATE USER studentapp WITH PASSWORD 'studentapp';
GRANT ALL PRIVILEGES ON DATABASE studentdb TO studentapp;

CREATE TABLE IF NOT EXISTS students (
id BIGSERIAL PRIMARY KEY,
first_name VARCHAR(100) NOT NULL,
last_name VARCHAR(100) NOT NULL,
email VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS courses (
id BIGSERIAL PRIMARY KEY,
code VARCHAR(50)  NOT NULL UNIQUE,
name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS enrollments (
student_id BIGINT NOT NULL,
course_id BIGINT NOT NULL,
grade VARCHAR(20),
CONSTRAINT pk_enrollments PRIMARY KEY (student_id, course_id),
CONSTRAINT fk_enroll_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
CONSTRAINT fk_enroll_course FOREIGN KEY (course_id)  REFERENCES courses(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_enroll_student ON enrollments(student_id);
CREATE INDEX IF NOT EXISTS idx_enroll_course ON enrollments(course_id);

##application.properties

spring.datasource.url=jdbc:postgresql://localhost:5432/studentdb
spring.datasource.username=studentapp
spring.datasource.password=studentapp
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

spring.web.cors.allowed-origins=http://localhost:4200
spring.web.cors.allowed-methods=GET,POST,PUT,PATCH,DELETE,OPTIONS

mvn spring-boot:run

Base URL: http://localhost:8080 (all routes under /api)

Endpoints (overview)

Students

GET /api/students

POST /api/students

PUT /api/students/{id}

DELETE /api/students/{id}

Courses

GET /api/courses

POST /api/courses

DELETE /api/courses/{id}

Enrollments

GET /api/enrollments/student/{studentId}

POST /api/enrollments — { studentId, courseId }

PATCH /api/enrollments/grade — { studentId, courseId, grade } (grade is free-text or null)

DELETE /api/enrollments?studentId=&courseId=

Structure (short)
controller/ # StudentController, CourseController, EnrollmentController
service/ # StudentService, CourseService, EnrollmentService
repository/ # JPA repositories
domain/ # entities
dto/ # EnrollmentDto, StudentCourseDto

Notes

CORS allows the Angular app at http://localhost:4200.

Controllers use explicit @PathVariable("...")/@RequestParam("...") names.