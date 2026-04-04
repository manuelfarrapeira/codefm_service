package org.web.codefm.domain.repository.teachernotebook;

import org.web.codefm.domain.entity.teachernotebook.GroupAssignmentGrade;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for group assignment grade data access operations.
 * Provides methods to retrieve and manage grades for group assignments.
 */
public interface GroupAssignmentGradeRepository {

    /**
     * Finds all active grades for a specific group assignment.
     *
     * @param assignmentId The unique identifier of the group assignment
     * @return List of active grades belonging to the specified assignment
     */
    List<GroupAssignmentGrade> findByAssignmentId(Integer assignmentId);

    /**
     * Finds an active grade for a specific group assignment and group.
     *
     * @param assignmentId The unique identifier of the group assignment
     * @param groupId      The unique identifier of the student group
     * @return Optional containing the grade if found
     */
    Optional<GroupAssignmentGrade> findByAssignmentIdAndGroupId(Integer assignmentId, Integer groupId);

    /**
     * Saves a new group assignment grade.
     *
     * @param grade The grade to save
     * @return The saved grade with generated ID
     */
    GroupAssignmentGrade save(GroupAssignmentGrade grade);

    /**
     * Updates an existing group assignment grade.
     *
     * @param grade The grade with updated data
     * @return The updated grade
     */
    GroupAssignmentGrade update(GroupAssignmentGrade grade);

    /**
     * Soft-deletes a grade by setting its deletion date.
     *
     * @param gradeId The unique identifier of the grade
     */
    void softDeleteById(Integer gradeId);

    /**
     * Soft-deletes all grades belonging to a group assignment.
     *
     * @param assignmentId The unique identifier of the group assignment
     */
    void softDeleteByGroupAssignmentId(Integer assignmentId);

    /**
     * Soft-deletes all grades belonging to a list of group assignments.
     *
     * @param assignmentIds The list of group assignment IDs
     */
    void softDeleteByGroupAssignmentIds(List<Integer> assignmentIds);

    /**
     * Soft-deletes all grades belonging to a student group.
     *
     * @param groupId The unique identifier of the student group
     */
    void softDeleteByGroupId(Integer groupId);

    /**
     * Soft-deletes all grades belonging to a list of student groups.
     *
     * @param groupIds The list of student group IDs
     */
    void softDeleteByGroupIds(List<Integer> groupIds);
}

