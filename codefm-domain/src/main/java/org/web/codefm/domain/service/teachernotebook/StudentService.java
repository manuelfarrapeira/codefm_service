package org.web.codefm.domain.service.teachernotebook;

import org.springframework.web.multipart.MultipartFile;
import org.web.codefm.domain.entity.teachernotebook.Student;

import java.util.List;

/**
 * Service interface for student business logic operations.
 * Acts as an intermediary between use cases and repositories.
 */
public interface StudentService {

    /**
     * Creates a new student with validation.
     * The teacher ID is obtained from the session user.
     *
     * @param student The student to create
     * @return The created student
     * @throws org.web.codefm.domain.exception.StudentValidationException if validation fails
     */
    Student createStudent(Student student);

    /**
     * Updates an existing student with validation.
     * The teacher ID is obtained from the session user.
     *
     * @param id      The unique identifier of the student
     * @param student The student data to update
     * @return The updated student
     * @throws org.web.codefm.domain.exception.StudentValidationException if validation fails
     */
    Student updateStudent(Integer id, Student student);

    /**
     * Performs soft delete on a student.
     * The teacher ID is obtained from the session user.
     *
     * @param id The unique identifier of the student
     */
    void softDeleteStudent(Integer id);

    /**
     * Saves a student photo.
     * The teacher ID is obtained from the session user.
     *
     * @param studentId The unique identifier of the student
     * @param file      The photo file
     * @return The path where the photo was saved
     * @throws org.web.codefm.domain.exception.StudentPhotoUploadException if upload fails
     */
    String saveStudentPhoto(Integer studentId, MultipartFile file);

    /**
     * Searches students by filters for the authenticated teacher. At least one filter must be provided.
     * The teacher ID is obtained from the session user.
     *
     * @param id       Student ID (optional)
     * @param name     Student name (optional, partial match)
     * @param surnames Student surnames (optional, partial match)
     * @return List of students matching the filters
     * @throws org.web.codefm.domain.exception.teachernotebook.StudentSearchValidationException if no filters are provided
     */
    List<Student> searchStudents(Integer id, String name, String surnames);
}
