package org.web.codefm.domain.service.teachernotebook;

import org.web.codefm.domain.entity.teachernotebook.ExerciseStudentGrade;

import java.util.List;

/**
 * Service interface for exercise student grade business logic operations.
 * Acts as an intermediary between use cases and repositories.
 */
public interface ExerciseStudentGradeService {

    /**
     * Retrieves all active student grades for exercises belonging to a specific class.
     *
     * @param classId The unique identifier of the class
     * @return List of active student grades for the specified class
     */
    List<ExerciseStudentGrade> getGradesByClassId(Integer classId);

    /**
     * Retrieves all active student grades for a specific student in a specific class.
     *
     * @param classId   The unique identifier of the class
     * @param studentId The unique identifier of the student
     * @return List of active student grades for the specified student and class
     */
    List<ExerciseStudentGrade> getGradesByClassIdAndStudentId(Integer classId, Integer studentId);

    /**
     * Creates a new student grade for an exercise.
     *
     * @param exerciseId The unique identifier of the exercise
     * @param grade      The student grade data to create
     * @return The created student grade
     */
    ExerciseStudentGrade createGrade(Integer exerciseId, ExerciseStudentGrade grade);

    /**
     * Updates an existing student grade.
     *
     * @param id    The unique identifier of the student grade to update
     * @param grade The student grade data with updated fields
     * @return The updated student grade
     */
    ExerciseStudentGrade updateGrade(Integer id, ExerciseStudentGrade grade);

    /**
     * Soft-deletes a student grade by setting its deletion date.
     *
     * @param id The unique identifier of the student grade to delete
     */
    void deleteGrade(Integer id);
}

