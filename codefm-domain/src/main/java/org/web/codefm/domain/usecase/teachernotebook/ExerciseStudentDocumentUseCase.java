package org.web.codefm.domain.usecase.teachernotebook;

import org.springframework.web.multipart.MultipartFile;
import org.web.codefm.domain.entity.teachernotebook.ExerciseStudentDocument;

/**
 * Interface that defines exercise student document operations for teachers.
 * Handles document upload, download, update and deletion for student grades.
 */
public interface ExerciseStudentDocumentUseCase {

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
}

