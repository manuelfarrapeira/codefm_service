package org.web.codefm.domain.service.teachernotebook;

import org.web.codefm.domain.entity.teachernotebook.StudentClass;

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
     * Validates ownership and returns the active student-class association.
     * Throws an exception if the class, student or association is not found or not owned by the teacher.
     *
     * @param classId   The unique identifier of the class
     * @param studentId The unique identifier of the student
     * @return The active StudentClass association
     * @throws org.web.codefm.domain.exception.teachernotebook.ClassNotFoundException        if the class is not found
     * @throws org.web.codefm.domain.exception.teachernotebook.StudentNotFoundException      if the student is not found
     * @throws org.web.codefm.domain.exception.teachernotebook.StudentClassNotFoundException if the association is not found
     */
    StudentClass findActiveAssociation(Integer classId, Integer studentId);

    /**
     * Removes a student from a class (soft delete).
     * The teacher ID is obtained from the session user.
     *
     * @param classId   The unique identifier of the class
     * @param studentId The unique identifier of the student
     */
    void removeStudentFromClass(Integer classId, Integer studentId);
}

