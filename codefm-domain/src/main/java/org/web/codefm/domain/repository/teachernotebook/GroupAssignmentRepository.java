package org.web.codefm.domain.repository.teachernotebook;

import org.web.codefm.domain.entity.teachernotebook.GroupAssignment;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for group assignment data access operations.
 * Provides methods to retrieve and manage group assignments linked to classes.
 */
public interface GroupAssignmentRepository {

    /**
     * Finds all active group assignments for a specific class.
     *
     * @param classId The unique identifier of the class
     * @return List of active group assignments belonging to the specified class
     */
    List<GroupAssignment> findByClassId(Integer classId);

    /**
     * Finds a group assignment by its ID and validates teacher ownership.
     *
     * @param assignmentId The unique identifier of the group assignment
     * @param teacherId    The unique identifier of the teacher
     * @return Optional containing the assignment if found and owned by the teacher
     */
    Optional<GroupAssignment> findByIdAndTeacherId(Integer assignmentId, Integer teacherId);

    /**
     * Saves a new group assignment.
     *
     * @param groupAssignment The group assignment to save
     * @return The saved group assignment with generated ID
     */
    GroupAssignment save(GroupAssignment groupAssignment);

    /**
     * Soft-deletes a group assignment by setting its deletion date.
     *
     * @param assignmentId The unique identifier of the group assignment
     */
    void softDeleteById(Integer assignmentId);

    /**
     * Soft-deletes all group assignments belonging to a class.
     *
     * @param classId The unique identifier of the class
     */
    void softDeleteByClassId(Integer classId);

    /**
     * Finds all active group assignment IDs belonging to a class.
     *
     * @param classId The unique identifier of the class
     * @return List of active group assignment IDs
     */
    List<Integer> findActiveIdsByClassId(Integer classId);
}

