package org.web.codefm.domain.repository.teachernotebook;

import org.web.codefm.domain.entity.teachernotebook.Exercise;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for exercise data access operations.
 * Provides methods to retrieve and manage exercises linked to subject-class associations.
 */
public interface ExerciseRepository {

    /**
     * Finds all active exercises for a specific class.
     *
     * @param classId The unique identifier of the class
     * @return List of active exercises belonging to the specified class
     */
    List<Exercise> findByClassId(Integer classId);

    /**
     * Finds an active exercise by its ID and validates teacher ownership.
     *
     * @param id        The unique identifier of the exercise
     * @param teacherId The unique identifier of the teacher
     * @return An Optional containing the exercise if found and owned by the teacher
     */
    Optional<Exercise> findByIdAndTeacherId(Integer id, Integer teacherId);

    /**
     * Saves a new exercise.
     *
     * @param exercise The exercise to save
     * @return The saved exercise with generated ID
     */
    Exercise save(Exercise exercise);

    /**
     * Updates an existing exercise.
     *
     * @param exercise The exercise with updated data
     * @return The updated exercise
     */
    Exercise update(Exercise exercise);

    /**
     * Soft-deletes an exercise by setting its deletion date.
     *
     * @param id The unique identifier of the exercise to soft-delete
     */
    void softDelete(Integer id);

    /**
     * Checks if a subject-class association exists, is active, and belongs to the specified teacher.
     *
     * @param subjectClassId The unique identifier of the subject-class association
     * @param teacherId      The unique identifier of the teacher
     * @return true if the subject-class association exists, is active, and belongs to the teacher
     */
    boolean subjectClassBelongsToTeacher(Integer subjectClassId, Integer teacherId);

    /**
     * Soft-deletes all exercises for the given subject-class association IDs.
     *
     * @param subjectClassIds The list of subject-class association IDs
     */
    void softDeleteBySubjectClassIds(List<Integer> subjectClassIds);

    /**
     * Finds all active exercise IDs for the given subject-class association IDs.
     *
     * @param subjectClassIds The list of subject-class association IDs
     * @return List of active exercise IDs
     */
    List<Integer> findActiveIdsBySubjectClassIds(List<Integer> subjectClassIds);

    /**
     * Calculates the sum of percentage grades for all active exercises
     * in a given subject-class association and quarter.
     *
     * @param subjectClassId The unique identifier of the subject-class association
     * @param quarter        The quarter number (1, 2 or 3)
     * @return The sum of percentage grades, or 0 if no exercises exist
     */
    Integer sumPercentageGradeBySubjectClassIdAndQuarter(Integer subjectClassId, Integer quarter);

    /**
     * Calculates the sum of percentage grades for all active exercises
     * in a given subject-class association and quarter, excluding a specific exercise.
     *
     * @param subjectClassId The unique identifier of the subject-class association
     * @param quarter        The quarter number (1, 2 or 3)
     * @param excludeId      The ID of the exercise to exclude from the sum
     * @return The sum of percentage grades excluding the specified exercise, or 0 if no exercises exist
     */
    Integer sumPercentageGradeBySubjectClassIdAndQuarterExcludingId(Integer subjectClassId, Integer quarter, Integer excludeId);
}
