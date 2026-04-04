package org.web.codefm.domain.usecase.teachernotebook;

import org.springframework.web.multipart.MultipartFile;
import org.web.codefm.domain.entity.teachernotebook.GroupAssignmentDocument;

/**
 * Interface that defines group assignment document operations for teachers.
 * Handles document upload, download and deletion.
 */
public interface GroupAssignmentDocumentUseCase {

    /**
     * Uploads a document for a group assignment (assignment-level).
     *
     * @param assignmentId The unique identifier of the group assignment
     * @param file         The document file to upload
     * @param description  Optional description for the document
     * @return The created document
     */
    GroupAssignmentDocument uploadAssignmentDocument(Integer assignmentId, MultipartFile file, String description);

    /**
     * Uploads a document for a group in an assignment (group-level).
     *
     * @param assignmentId The unique identifier of the group assignment
     * @param groupId      The unique identifier of the student group
     * @param file         The document file to upload
     * @param description  Optional description for the document
     * @return The created document
     */
    GroupAssignmentDocument uploadGroupDocument(Integer assignmentId, Integer groupId, MultipartFile file, String description);

    /**
     * Downloads a document by its ID.
     *
     * @param assignmentId The unique identifier of the group assignment
     * @param documentId   The unique identifier of the document
     * @return The document file bytes
     */
    byte[] downloadDocument(Integer assignmentId, Integer documentId);

    /**
     * Gets the filename of a document.
     *
     * @param assignmentId The unique identifier of the group assignment
     * @param documentId   The unique identifier of the document
     * @return The document filename stored on disk
     */
    String getDocumentFilename(Integer assignmentId, Integer documentId);

    /**
     * Deletes a document (hard delete: removes record and file from disk).
     *
     * @param assignmentId The unique identifier of the group assignment
     * @param documentId   The unique identifier of the document
     */
    void deleteDocument(Integer assignmentId, Integer documentId);
}

