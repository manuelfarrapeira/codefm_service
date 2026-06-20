package org.web.codefm.domain.usecase.teachernotebook;

import org.springframework.web.multipart.MultipartFile;
import org.web.codefm.domain.entity.teachernotebook.ExerciseDocument;

/**
 * Interface that defines exercise document operations for teachers.
 * Handles document upload, download, update and deletion.
 */
public interface ExerciseDocumentUseCase {

    /**
     * Uploads a document for an exercise.
     *
     * @param exerciseId  The unique identifier of the exercise
     * @param file        The document file to upload
     * @param description Optional description for the document
     * @return The created exercise document
     */
    ExerciseDocument uploadDocument(Integer exerciseId, MultipartFile file, String description);

    /**
     * Downloads a document by its ID for a specific exercise.
     *
     * @param exerciseId The unique identifier of the exercise
     * @param documentId The unique identifier of the document
     * @return The document file bytes
     */
    byte[] downloadDocument(Integer exerciseId, Integer documentId);

    /**
     * Gets the filename of a document.
     *
     * @param exerciseId The unique identifier of the exercise
     * @param documentId The unique identifier of the document
     * @return The document filename stored on disk
     */
    String getDocumentFilename(Integer exerciseId, Integer documentId);

    /**
     * Updates the description of a document.
     *
     * @param exerciseId  The unique identifier of the exercise
     * @param documentId  The unique identifier of the document
     * @param description The new description
     * @return The updated exercise document
     */
    ExerciseDocument updateDescription(Integer exerciseId, Integer documentId, String description);

    /**
     * Deletes a document (hard delete: removes record and file from disk).
     *
     * @param exerciseId The unique identifier of the exercise
     * @param documentId The unique identifier of the document
     */
    void deleteDocument(Integer exerciseId, Integer documentId);
}

