package org.web.codefm.domain.repository.teachernotebook;

import org.web.codefm.domain.entity.teachernotebook.ClassRubric;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for class-rubric assignment data access operations.
 * Provides methods to manage the association between classes and skill rubrics.
 */
public interface ClassRubricRepository {

    /**
     * Finds all active class-rubric assignments for a specific class,
     * enriched with rubric title, skill ID and active criteria list.
     *
     * @param classId The unique identifier of the class
     * @return List of active ClassRubric assignments with display fields populated
     */
    List<ClassRubric> findByClassId(Integer classId);

    /**
     * Finds an active class-rubric assignment by its ID, validating teacher ownership
     * through the class → school → teacher chain.
     *
     * @param id        The unique identifier of the class-rubric assignment
     * @param teacherId The unique identifier of the teacher
     * @return An Optional containing the ClassRubric if found and owned by the teacher
     */
    Optional<ClassRubric> findByIdAndTeacherId(Integer id, Integer teacherId);

    /**
     * Saves a new class-rubric assignment.
     *
     * @param classRubric The ClassRubric object to save
     * @return The saved ClassRubric with generated ID
     */
    ClassRubric save(ClassRubric classRubric);

    /**
     * Soft-deletes a class-rubric assignment by setting its deletion date.
     *
     * @param id The ID of the class-rubric assignment to soft-delete
     */
    void softDeleteById(Integer id);

    /**
     * Soft-deletes all class-rubric assignments for a specific class.
     * Used in cascade when a class is soft-deleted.
     *
     * @param classId The ID of the class whose rubric assignments should be soft-deleted
     */
    void softDeleteByClassId(Integer classId);

    /**
     * Soft-deletes all class-rubric assignments for a specific rubric.
     * Used in cascade when a rubric is soft-deleted.
     *
     * @param rubricId The ID of the rubric whose class assignments should be soft-deleted
     */
    void softDeleteByRubricId(Integer rubricId);

    /**
     * Finds all active class-rubric assignment IDs for a specific class.
     * Used to determine children for cascade soft-delete.
     *
     * @param classId The unique identifier of the class
     * @return List of active class-rubric assignment IDs
     */
    List<Integer> findActiveIdsByClassId(Integer classId);

    /**
     * Finds all active class-rubric assignment IDs for a specific rubric.
     * Used to determine children for cascade soft-delete when a rubric is deleted.
     *
     * @param rubricId The unique identifier of the rubric
     * @return List of active class-rubric assignment IDs
     */
    List<Integer> findActiveIdsByRubricId(Integer rubricId);

    /**
     * Checks whether an active assignment exists for the given class and rubric combination.
     *
     * @param classId  The unique identifier of the class
     * @param rubricId The unique identifier of the rubric
     * @return true if an active assignment already exists, false otherwise
     */
    boolean existsByClassIdAndRubricIdAndDeletionDateIsNull(Integer classId, Integer rubricId);
}

