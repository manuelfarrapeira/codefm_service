package org.web.codefm.domain.repository.teachernotebook;

import org.web.codefm.domain.entity.teachernotebook.ExerciseDocument;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for exercise document data access operations.
 * Provides methods to retrieve and manage documents linked to exercises.
 */
public interface ExerciseDocumentRepository {

    /**
     * Saves a new exercise document.
     *
     * @param exerciseDocument The exercise document to save
     * @return The saved exercise document with generated ID
     */
    ExerciseDocument save(ExerciseDocument exerciseDocument);

    /**
     * Updates an existing exercise document.
     *
     * @param exerciseDocument The exercise document with updated data
     * @return The updated exercise document
     */
    ExerciseDocument update(ExerciseDocument exerciseDocument);

    /**
     * Finds all documents for a specific exercise.
     *
     * @param exerciseId The unique identifier of the exercise
     * @return List of documents belonging to the specified exercise
     */
    List<ExerciseDocument> findByExerciseId(Integer exerciseId);

    /**
     * Finds a document by its ID.
     *
     * @param id The unique identifier of the document
     * @return An Optional containing the document if found
     */
    Optional<ExerciseDocument> findById(Integer id);

    /**
     * Deletes a document by its ID (hard delete).
     *
     * @param id The unique identifier of the document to delete
     */
    void deleteById(Integer id);

    /**
     * Deletes all documents for a specific exercise (hard delete).
     *
     * @param exerciseId The unique identifier of the exercise
     */
    void deleteByExerciseId(Integer exerciseId);

    /**
     * Deletes all documents for the given exercise IDs (hard delete).
     *
     * @param exerciseIds The list of exercise IDs
     */
    void deleteByExerciseIds(List<Integer> exerciseIds);

    /**
     * Finds all documents for the given exercise IDs.
     *
     * @param exerciseIds The list of exercise IDs
     * @return List of documents belonging to the specified exercises
     */
    List<ExerciseDocument> findByExerciseIds(List<Integer> exerciseIds);
}

