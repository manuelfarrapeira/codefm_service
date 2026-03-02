package org.web.codefm.domain.repository.teachernotebook;

import org.web.codefm.domain.entity.teachernotebook.StudentClass;

import java.util.List;
import java.util.Map;
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
     * Finds a student-class association by its ID.
     *
     * @param id The unique identifier of the association
     * @return Optional containing the association if found
     */
    Optional<StudentClass> findById(Integer id);

    /**
     * Finds all active class IDs for a student.
     *
     * @param studentId The unique identifier of the student
     * @return List of class IDs the student belongs to (excluding soft-deleted associations)
     */
    List<Integer> findClassIdsByStudentId(Integer studentId);

    /**
     * Finds all active student-class associations for students of a teacher.
     * Returns a map where key is studentId and value is list of classIds.
     *
     * @param teacherId The unique identifier of the teacher
     * @return Map of studentId to list of classIds
     */
    Map<Integer, List<Integer>> findClassIdsByTeacherId(Integer teacherId);

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

    /**
     * Soft-deletes all student-class associations for a specific class.
     *
     * @param classId The unique identifier of the class
     */
    void softDeleteByClassId(Integer classId);

    /**
     * Soft-deletes all student-class associations for a specific student.
     *
     * @param studentId The unique identifier of the student
     */
    void softDeleteByStudentId(Integer studentId);
}
