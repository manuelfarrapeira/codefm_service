package org.web.codefm.domain.repository.teachernotebook;

import org.web.codefm.domain.entity.teachernotebook.ExerciseStudentDocument;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for exercise student document data access operations.
 * Provides methods to retrieve and manage documents linked to student grades.
 */
public interface ExerciseStudentDocumentRepository {

    /**
     * Saves a new exercise student document.
     *
     * @param document The exercise student document to save
     * @return The saved document with generated ID
     */
    ExerciseStudentDocument save(ExerciseStudentDocument document);

    /**
     * Updates an existing exercise student document.
     *
     * @param document The exercise student document with updated data
     * @return The updated document
     */
    ExerciseStudentDocument update(ExerciseStudentDocument document);

    /**
     * Finds all documents for a specific grade.
     *
     * @param gradeId The unique identifier of the grade
     * @return List of documents belonging to the specified grade
     */
    List<ExerciseStudentDocument> findByGradeId(Integer gradeId);

    /**
     * Finds a document by its ID.
     *
     * @param id The unique identifier of the document
     * @return An Optional containing the document if found
     */
    Optional<ExerciseStudentDocument> findById(Integer id);

    /**
     * Deletes a document by its ID (hard delete).
     *
     * @param id The unique identifier of the document to delete
     */
    void deleteById(Integer id);

    /**
     * Deletes all documents for a specific grade (hard delete).
     *
     * @param gradeId The unique identifier of the grade
     */
    void deleteByGradeId(Integer gradeId);

    /**
     * Deletes all documents for the given grade IDs (hard delete).
     *
     * @param gradeIds The list of grade IDs
     */
    void deleteByGradeIds(List<Integer> gradeIds);

    /**
     * Finds all documents for the given grade IDs.
     *
     * @param gradeIds The list of grade IDs
     * @return List of documents belonging to the specified grades
     */
    List<ExerciseStudentDocument> findByGradeIds(List<Integer> gradeIds);

    /**
     * Finds all documents linked to grades of a specific exercise.
     *
     * @param exerciseId The unique identifier of the exercise
     * @return List of documents linked to grades of the exercise
     */
    List<ExerciseStudentDocument> findByExerciseId(Integer exerciseId);

    /**
     * Finds all documents linked to grades of the given exercise IDs.
     *
     * @param exerciseIds The list of exercise IDs
     * @return List of documents linked to grades of the specified exercises
     */
    List<ExerciseStudentDocument> findByExerciseIds(List<Integer> exerciseIds);

    /**
     * Deletes all documents linked to grades of a specific exercise (hard delete).
     *
     * @param exerciseId The unique identifier of the exercise
     */
    void deleteByExerciseId(Integer exerciseId);

    /**
     * Deletes all documents linked to grades of the given exercise IDs (hard delete).
     *
     * @param exerciseIds The list of exercise IDs
     */
    void deleteByExerciseIds(List<Integer> exerciseIds);

    /**
     * Finds all documents linked to grades of a specific student.
     *
     * @param studentId The unique identifier of the student
     * @return List of documents linked to grades of the student
     */
    List<ExerciseStudentDocument> findByStudentId(Integer studentId);

    /**
     * Deletes all documents linked to grades of a specific student (hard delete).
     *
     * @param studentId The unique identifier of the student
     */
    void deleteByStudentId(Integer studentId);
}

