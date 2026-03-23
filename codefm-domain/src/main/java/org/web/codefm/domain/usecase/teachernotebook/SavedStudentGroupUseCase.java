package org.web.codefm.domain.usecase.teachernotebook;

import org.web.codefm.domain.entity.teachernotebook.SavedStudentGroup;

import java.util.List;

/**
 * Interface that defines saved student group operations for teachers.
 * Handles persisted student group data retrieval and management.
 */
public interface SavedStudentGroupUseCase {

    /**
     * Retrieves all saved groups for a specific class, including member details.
     *
     * @param classId The unique identifier of the class
     * @return List of saved student groups with their members
     */
    List<SavedStudentGroup> getSavedGroupsByClassId(Integer classId);

    /**
     * Creates multiple saved student groups for a class.
     *
     * @param classId The unique identifier of the class
     * @param groups  The list of groups to create
     * @return The list of created groups with generated IDs
     */
    List<SavedStudentGroup> createSavedGroups(Integer classId, List<SavedStudentGroup> groups);

    /**
     * Replaces all saved student groups for a class.
     * All students in the class must be assigned to a group and no student can appear in more than one group.
     *
     * @param classId The unique identifier of the class
     * @param groups  The new list of groups replacing the existing ones
     * @return The list of newly created groups with generated IDs
     */
    List<SavedStudentGroup> updateAllSavedGroups(Integer classId, List<SavedStudentGroup> groups);


    /**
     * Soft-deletes all saved student groups for a class and hard-deletes their members.
     *
     * @param classId The unique identifier of the class
     */
    void softDeleteSavedGroupsByClassId(Integer classId);
}
