package org.web.codefm.domain.repository.teachernotebook;

import org.web.codefm.domain.entity.teachernotebook.ExerciseStudentGrade;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for exercise student grade data access operations.
 * Provides methods to retrieve and manage student grades linked to exercises.
 */
public interface ExerciseStudentGradeRepository {

    /**
     * Finds all active student grades for exercises belonging to a specific class.
     *
     * @param classId The unique identifier of the class
     * @return List of active student grades for the specified class
     */
    List<ExerciseStudentGrade> findByClassId(Integer classId);

    /**
     * Finds all active student grades for a specific student in a specific class.
     *
     * @param classId   The unique identifier of the class
     * @param studentId The unique identifier of the student
     * @return List of active student grades for the specified student and class
     */
    List<ExerciseStudentGrade> findByClassIdAndStudentId(Integer classId, Integer studentId);

    /**
     * Finds an active student grade by its ID and validates teacher ownership.
     *
     * @param id        The unique identifier of the student grade
     * @param teacherId The unique identifier of the teacher
     * @return An Optional containing the student grade if found and owned by the teacher
     */
    Optional<ExerciseStudentGrade> findByIdAndTeacherId(Integer id, Integer teacherId);

    /**
     * Saves a new student grade.
     *
     * @param grade The student grade to save
     * @return The saved student grade with generated ID
     */
    ExerciseStudentGrade save(ExerciseStudentGrade grade);

    /**
     * Updates an existing student grade.
     *
     * @param grade The student grade with updated data
     * @return The updated student grade
     */
    ExerciseStudentGrade update(ExerciseStudentGrade grade);

    /**
     * Soft-deletes a student grade by setting its deletion date.
     *
     * @param id The unique identifier of the student grade to soft-delete
     */
    void softDelete(Integer id);

    /**
     * Checks if a student already has an active grade for a specific exercise.
     *
     * @param studentId  The unique identifier of the student
     * @param exerciseId The unique identifier of the exercise
     * @return true if the student already has an active grade for the exercise
     */
    boolean existsByStudentIdAndExerciseIdAndDeletionDateIsNull(Integer studentId, Integer exerciseId);

    /**
     * Soft-deletes all student grades for the given exercise IDs.
     *
     * @param exerciseIds The list of exercise IDs
     */
    void softDeleteByExerciseIds(List<Integer> exerciseIds);

    /**
     * Soft-deletes all grades for a specific student in all exercises belonging to a specific class.
     *
     * @param studentId The unique identifier of the student
     * @param classId   The unique identifier of the class
     */
    void softDeleteByStudentIdAndClassId(Integer studentId, Integer classId);

    /**
     * Soft-deletes all grades for a specific student across all exercises.
     *
     * @param studentId The unique identifier of the student
     */
    void softDeleteByStudentId(Integer studentId);
}

