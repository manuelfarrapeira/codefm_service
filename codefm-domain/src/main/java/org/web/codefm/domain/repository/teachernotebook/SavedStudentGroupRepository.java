package org.web.codefm.domain.repository.teachernotebook;

import org.web.codefm.domain.entity.teachernotebook.SavedStudentGroup;
import org.web.codefm.domain.entity.teachernotebook.SavedStudentGroupMember;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for saved student group data access operations.
 * Provides methods to retrieve and manage persisted student groups.
 */
public interface SavedStudentGroupRepository {

    /**
     * Finds all active saved groups for a class, including their members with student names.
     *
     * @param classId The unique identifier of the class
     * @return List of saved student groups with their members
     */
    List<SavedStudentGroup> findByClassIdWithMembers(Integer classId);

    /**
     * Finds a saved group by its ID and validates teacher ownership.
     *
     * @param groupId   The unique identifier of the group
     * @param teacherId The unique identifier of the teacher
     * @return Optional containing the group if found and owned by the teacher
     */
    Optional<SavedStudentGroup> findByIdAndTeacherId(Integer groupId, Integer teacherId);

    /**
     * Saves a new student group.
     *
     * @param group The group to save
     * @return The saved group with generated ID
     */
    SavedStudentGroup save(SavedStudentGroup group);

    /**
     * Saves multiple student groups in a batch.
     *
     * @param groups The list of groups to save
     * @return The saved groups with generated IDs, in the same order as the input
     */
    List<SavedStudentGroup> saveAll(List<SavedStudentGroup> groups);

    /**
     * Updates the name of an existing group directly, without a prior SELECT.
     *
     * @param id   The unique identifier of the group
     * @param name The new name
     */
    void updateName(Integer id, String name);

    /**
     * Saves a list of members for a group.
     *
     * @param members The members to save
     */
    void saveMembers(List<SavedStudentGroupMember> members);

    /**
     * Soft-deletes a group by setting its deletion date.
     *
     * @param groupId The unique identifier of the group
     */
    void softDeleteById(Integer groupId);

    /**
     * Hard-deletes all members of a group.
     *
     * @param groupId The unique identifier of the group
     */
    void hardDeleteMembersByGroupId(Integer groupId);

    /**
     * Soft-deletes all groups belonging to a class.
     *
     * @param classId The unique identifier of the class
     */
    void softDeleteByClassId(Integer classId);

    /**
     * Hard-deletes all members of the specified groups.
     *
     * @param groupIds The list of group IDs
     */
    void hardDeleteMembersByGroupIds(List<Integer> groupIds);

    /**
     * Finds all active group IDs belonging to a class.
     *
     * @param classId The unique identifier of the class
     * @return List of active group IDs
     */
    List<Integer> findActiveIdsByClassId(Integer classId);
}
