package org.web.codefm.domain.service.teachernotebook;

import org.web.codefm.domain.entity.teachernotebook.Exercise;

import java.util.List;

/**
 * Service interface for exercise business logic operations.
 * Acts as an intermediary between use cases and repositories.
 */
public interface ExerciseService {

    /**
     * Retrieves all active exercises for a specific class.
     *
     * @param classId The unique identifier of the class
     * @return List of active exercises belonging to the specified class
     */
    List<Exercise> getExercisesByClassId(Integer classId);

    /**
     * Creates a new exercise for a subject-class association.
     *
     * @param subjectClassId The unique identifier of the subject-class association
     * @param exercise       The exercise data to create
     * @return The created exercise
     */
    Exercise createExercise(Integer subjectClassId, Exercise exercise);

    /**
     * Updates an existing exercise.
     *
     * @param id       The unique identifier of the exercise to update
     * @param exercise The exercise data with updated fields
     * @return The updated exercise
     */
    Exercise updateExercise(Integer id, Exercise exercise);

    /**
     * Soft-deletes an exercise by setting its deletion date.
     *
     * @param id The unique identifier of the exercise to delete
     */
    void deleteExercise(Integer id);
}

