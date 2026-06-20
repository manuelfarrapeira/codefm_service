package org.web.codefm.domain.service.teachernotebook;

import org.springframework.web.multipart.MultipartFile;
import org.web.codefm.domain.entity.teachernotebook.ExerciseStudentDocument;

import java.util.List;

/**
 * Service interface for exercise student document business logic operations.
 * Acts as an intermediary between use cases and repositories.
 */
public interface ExerciseStudentDocumentService {

    /**
     * Uploads a document for a student grade.
     *
     * @param gradeId     The unique identifier of the grade
     * @param file        The document file to upload
     * @param description Optional description for the document
     * @return The created exercise student document
     */
    ExerciseStudentDocument uploadDocument(Integer gradeId, MultipartFile file, String description);

    /**
     * Downloads a document by its ID for a specific grade.
     *
     * @param gradeId    The unique identifier of the grade
     * @param documentId The unique identifier of the document
     * @return The document file bytes
     */
    byte[] downloadDocument(Integer gradeId, Integer documentId);

    /**
     * Gets the filename of a document stored on disk.
     *
     * @param gradeId    The unique identifier of the grade
     * @param documentId The unique identifier of the document
     * @return The document filename stored on disk
     */
    String getDocumentFilename(Integer gradeId, Integer documentId);

    /**
     * Updates the description of a document.
     *
     * @param gradeId     The unique identifier of the grade
     * @param documentId  The unique identifier of the document
     * @param description The new description
     * @return The updated exercise student document
     */
    ExerciseStudentDocument updateDescription(Integer gradeId, Integer documentId, String description);

    /**
     * Deletes a document (hard delete: removes record and file from disk).
     *
     * @param gradeId    The unique identifier of the grade
     * @param documentId The unique identifier of the document
     */
    void deleteDocument(Integer gradeId, Integer documentId);

    /**
     * Deletes all documents for a specific grade (hard delete).
     * Used for cascade deletion when a grade is soft-deleted.
     *
     * @param gradeId The unique identifier of the grade
     */
    void deleteDocumentsByGradeId(Integer gradeId);

    /**
     * Deletes all documents for the given grade IDs (hard delete).
     * Used for cascade deletion when grades are soft-deleted in batch.
     *
     * @param gradeIds The list of grade IDs
     */
    void deleteDocumentsByGradeIds(List<Integer> gradeIds);

    /**
     * Deletes all documents linked to grades of a specific exercise (hard delete).
     * Used for cascade deletion when an exercise is soft-deleted.
     *
     * @param exerciseId The unique identifier of the exercise
     */
    void deleteDocumentsByExerciseId(Integer exerciseId);

    /**
     * Deletes all documents linked to grades of the given exercise IDs (hard delete).
     * Used for cascade deletion when exercises are soft-deleted in batch.
     *
     * @param exerciseIds The list of exercise IDs
     */
    void deleteDocumentsByExerciseIds(List<Integer> exerciseIds);

    /**
     * Deletes all documents linked to grades of a specific student (hard delete).
     * Used for cascade deletion when a student is soft-deleted.
     *
     * @param studentId The unique identifier of the student
     */
    void deleteDocumentsByStudentId(Integer studentId);

    /**
     * Deletes all documents linked to grades of a specific student in a specific class (hard delete).
     * Used for cascade deletion when a student is removed from a class.
     *
     * @param studentId The unique identifier of the student
     * @param classId   The unique identifier of the class
     */
    void deleteDocumentsByStudentIdAndClassId(Integer studentId, Integer classId);
}
