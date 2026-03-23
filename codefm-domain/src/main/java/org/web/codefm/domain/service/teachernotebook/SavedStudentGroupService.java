package org.web.codefm.domain.service.teachernotebook;

import org.web.codefm.domain.entity.teachernotebook.SavedStudentGroup;

import java.util.List;

/**
 * Service interface for saved student group business logic operations.
 * Acts as an intermediary between use cases and repositories.
 */
public interface SavedStudentGroupService {

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
     * @throws org.web.codefm.domain.exception.teachernotebook.ClassNotFoundException               if class not found or not owned
     * @throws org.web.codefm.domain.exception.teachernotebook.SavedStudentGroupValidationException if validation fails
     */
    List<SavedStudentGroup> updateAllSavedGroups(Integer classId, List<SavedStudentGroup> groups);


    /**
     * Soft-deletes all saved student groups for a class and hard-deletes their members.
     *
     * @param classId The unique identifier of the class
     * @throws org.web.codefm.domain.exception.teachernotebook.ClassNotFoundException if class not found or not owned
     */
    void softDeleteSavedGroupsByClassId(Integer classId);
}
