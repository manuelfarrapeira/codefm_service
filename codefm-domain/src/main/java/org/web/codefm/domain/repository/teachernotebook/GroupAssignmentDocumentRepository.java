package org.web.codefm.domain.repository.teachernotebook;

import org.web.codefm.domain.entity.teachernotebook.GroupAssignmentDocument;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for group assignment document data access operations.
 * Provides methods to retrieve and manage documents linked to group assignments.
 * All delete operations are hard deletes (records are physically removed).
 */
public interface GroupAssignmentDocumentRepository {

    /**
     * Finds all documents for a specific group assignment.
     *
     * @param assignmentId The unique identifier of the group assignment
     * @return List of documents belonging to the specified assignment
     */
    List<GroupAssignmentDocument> findByAssignmentId(Integer assignmentId);

    /**
     * Finds all documents for a specific group assignment and student group.
     *
     * @param assignmentId The unique identifier of the group assignment
     * @param groupId      The unique identifier of the student group
     * @return List of documents belonging to the specified assignment and group
     */
    List<GroupAssignmentDocument> findByAssignmentIdAndGroupId(Integer assignmentId, Integer groupId);

    /**
     * Finds a document by its ID.
     *
     * @param id The unique identifier of the document
     * @return Optional containing the document if found
     */
    Optional<GroupAssignmentDocument> findById(Integer id);

    /**
     * Saves a new group assignment document.
     *
     * @param document The document to save
     * @return The saved document with generated ID
     */
    GroupAssignmentDocument save(GroupAssignmentDocument document);

    /**
     * Deletes a document by its ID (hard delete).
     *
     * @param id The unique identifier of the document to delete
     */
    void deleteById(Integer id);

    /**
     * Deletes all documents for a specific group assignment (hard delete).
     *
     * @param assignmentId The unique identifier of the group assignment
     */
    void deleteByGroupAssignmentId(Integer assignmentId);

    /**
     * Deletes all documents for the given group assignment IDs (hard delete).
     *
     * @param assignmentIds The list of group assignment IDs
     */
    void deleteByGroupAssignmentIds(List<Integer> assignmentIds);

    /**
     * Deletes all documents for a specific student group (hard delete).
     *
     * @param groupId The unique identifier of the student group
     */
    void deleteByGroupId(Integer groupId);

    /**
     * Deletes all documents for the given student group IDs (hard delete).
     *
     * @param groupIds The list of student group IDs
     */
    void deleteByGroupIds(List<Integer> groupIds);

    /**
     * Finds all documents for the given group assignment IDs.
     *
     * @param assignmentIds The list of group assignment IDs
     * @return List of documents belonging to the specified assignments
     */
    List<GroupAssignmentDocument> findByGroupAssignmentIds(List<Integer> assignmentIds);

    /**
     * Finds all documents for the given student group IDs.
     *
     * @param groupIds The list of student group IDs
     * @return List of documents belonging to the specified groups
     */
    List<GroupAssignmentDocument> findByGroupIds(List<Integer> groupIds);
}

