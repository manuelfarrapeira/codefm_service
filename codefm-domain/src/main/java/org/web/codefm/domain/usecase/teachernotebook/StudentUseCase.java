package org.web.codefm.domain.usecase.teachernotebook;

import org.springframework.web.multipart.MultipartFile;
import org.web.codefm.domain.entity.teachernotebook.Student;

import java.util.List;

/**
 * Interface that defines student operations.
 * Handles student data creation, modification and deletion.
 */
public interface StudentUseCase {

    /**
     * Creates a new student.
     *
     * @param student The student to create
     * @return The created student
     */
    Student createStudent(Student student);

    /**
     * Updates an existing student.
     *
     * @param id      The unique identifier of the student
     * @param student The student data to update
     * @return The updated student
     */
    Student updateStudent(Integer id, Student student);

    /**
     * Performs soft delete on a student.
     *
     * @param id The unique identifier of the student
     */
    void softDeleteStudent(Integer id);

    /**
     * Uploads a photo for a student.
     *
     * @param studentId The unique identifier of the student
     * @param file      The photo file
     * @return The path where the photo was saved
     */
    String uploadStudentPhoto(Integer studentId, MultipartFile file);

    /**
     * Downloads a photo for a student.
     *
     * @param studentId The unique identifier of the student
     * @return The photo file bytes
     */
    byte[] downloadStudentPhoto(Integer studentId);

    /**
     * Deletes a photo for a student.
     *
     * @param studentId The unique identifier of the student
     */
    void deleteStudentPhoto(Integer studentId);

    /**
     * Searches students by filters.
     *
     * @param id       Student ID (optional)
     * @param name     Student name (optional)
     * @param surnames Student surnames (optional)
     * @return List of students matching the filters
     */
    List<Student> searchStudents(Integer id, String name, String surnames);

    /**
     * Retrieves all students for the authenticated teacher.
     *
     * @return List of all students belonging to the teacher
     */
    List<Student> getAllStudents();
}
