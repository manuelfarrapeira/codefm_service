package org.web.codefm.domain.repository.teachernotebook;

import org.web.codefm.domain.entity.teachernotebook.StudentClass;

import java.util.Optional;

/**
 * Repository interface for student-class association data access operations.
 * Provides methods to manage the relationship between students and classes.
 */
public interface StudentClassRepository {

    /**
     * Finds an association between a student and a class (including soft-deleted).
     *
     * @param classId   The unique identifier of the class
     * @param studentId The unique identifier of the student
     * @return Optional containing the association if found
     */
    Optional<StudentClass> findByClassIdAndStudentId(Integer classId, Integer studentId);

    /**
     * Saves a new student-class association.
     *
     * @param studentClass The association to save
     * @return The saved association with generated ID
     */
    StudentClass save(StudentClass studentClass);

    /**
     * Updates an existing student-class association.
     *
     * @param studentClass The association to update
     * @return The updated association
     */
    StudentClass update(StudentClass studentClass);

    /**
     * Performs soft delete on a student-class association.
     *
     * @param classId   The unique identifier of the class
     * @param studentId The unique identifier of the student
     */
    void softDelete(Integer classId, Integer studentId);
}

