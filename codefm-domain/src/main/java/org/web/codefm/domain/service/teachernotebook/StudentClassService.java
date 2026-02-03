package org.web.codefm.domain.service.teachernotebook;

/**
 * Service interface for student-class association business logic operations.
 * Acts as an intermediary between use cases and repositories.
 */
public interface StudentClassService {

    /**
     * Associates a student with a class. If the association exists but is deleted,
     * it will be reactivated instead of creating a new one.
     * The teacher ID is obtained from the session user.
     *
     * @param classId   The unique identifier of the class
     * @param studentId The unique identifier of the student
     * @throws org.web.codefm.domain.exception.teachernotebook.StudentClassValidationException if association already exists
     */
    void addStudentToClass(Integer classId, Integer studentId);

    /**
     * Removes a student from a class (soft delete).
     * The teacher ID is obtained from the session user.
     *
     * @param classId   The unique identifier of the class
     * @param studentId The unique identifier of the student
     */
    void removeStudentFromClass(Integer classId, Integer studentId);
}

