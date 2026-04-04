package org.web.codefm.domain.usecase.teachernotebook;

import org.web.codefm.domain.entity.teachernotebook.GroupAssignment;
import org.web.codefm.domain.entity.teachernotebook.GroupAssignmentGrade;

import java.util.List;

/**
 * Interface that defines group assignment operations for teachers.
 * Handles assignment CRUD, grade management and cascade deletion.
 */
public interface GroupAssignmentUseCase {

    /**
     * Retrieves all active group assignments for a class.
     *
     * @param classId The unique identifier of the class
     * @return List of group assignments
     */
    List<GroupAssignment> getAssignmentsByClassId(Integer classId);

    /**
     * Creates a new group assignment for a class.
     *
     * @param classId    The unique identifier of the class
     * @param assignment The group assignment to create
     * @return The created group assignment
     */
    GroupAssignment createAssignment(Integer classId, GroupAssignment assignment);

    /**
     * Updates an existing group assignment.
     *
     * @param assignmentId The unique identifier of the group assignment
     * @param assignment   The group assignment with updated data
     * @return The updated group assignment
     */
    GroupAssignment updateAssignment(Integer assignmentId, GroupAssignment assignment);

    /**
     * Soft-deletes a group assignment with cascade deletion of grades and documents.
     *
     * @param assignmentId The unique identifier of the group assignment
     */
    void softDeleteAssignment(Integer assignmentId);

    /**
     * Retrieves all active grades for a group assignment.
     *
     * @param assignmentId The unique identifier of the group assignment
     * @return List of grades
     */
    List<GroupAssignmentGrade> getGradesByAssignmentId(Integer assignmentId);

    /**
     * Creates or updates a grade for a group in an assignment.
     *
     * @param assignmentId The unique identifier of the group assignment
     * @param groupId      The unique identifier of the student group
     * @param grade        The grade value (0-10)
     * @return The created or updated grade
     */
    GroupAssignmentGrade createOrUpdateGrade(Integer assignmentId, Integer groupId, Double grade);

    /**
     * Soft-deletes a grade for a group in an assignment.
     *
     * @param assignmentId The unique identifier of the group assignment
     * @param groupId      The unique identifier of the student group
     */
    void deleteGrade(Integer assignmentId, Integer groupId);
}

