package org.web.codefm.domain.service.teachernotebook;

import org.springframework.web.multipart.MultipartFile;
import org.web.codefm.domain.entity.teachernotebook.GroupAssignmentDocument;

import java.util.List;

/**
 * Service interface for group assignment document business logic operations.
 * Handles document upload, download, deletion and cascade operations.
 */
public interface GroupAssignmentDocumentService {

    /**
     * Uploads a document for a group assignment.
     *
     * @param assignmentId  The unique identifier of the group assignment
     * @param groupId       The unique identifier of the student group (null for assignment-level documents)
     * @param file          The document file to upload
     * @param description   Optional description for the document
     * @param groupDocument Whether this is a group-specific document
     * @return The created document
     */
    GroupAssignmentDocument uploadDocument(Integer assignmentId, Integer groupId, MultipartFile file, String description, Boolean groupDocument);

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

    /**
     * Deletes all documents for a specific group assignment (hard delete + file removal).
     *
     * @param assignmentId The unique identifier of the group assignment
     */
    void deleteDocumentsByGroupAssignmentId(Integer assignmentId);

    /**
     * Deletes all documents for the given group assignment IDs (hard delete + file removal).
     *
     * @param assignmentIds The list of group assignment IDs
     */
    void deleteDocumentsByGroupAssignmentIds(List<Integer> assignmentIds);

    /**
     * Deletes all documents for a specific student group (hard delete + file removal).
     *
     * @param groupId The unique identifier of the student group
     */
    void deleteDocumentsByGroupId(Integer groupId);

    /**
     * Deletes all documents for the given student group IDs (hard delete + file removal).
     *
     * @param groupIds The list of student group IDs
     */
    void deleteDocumentsByGroupIds(List<Integer> groupIds);
}

