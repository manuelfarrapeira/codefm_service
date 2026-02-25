package org.web.codefm.domain.service.teachernotebook;

import org.web.codefm.domain.entity.teachernotebook.ClassWithSubjects;
import org.web.codefm.domain.entity.teachernotebook.SubjectClassDetail;

import java.util.List;

/**
 * Service interface for subject-class association business logic operations.
 * Acts as an intermediary between use cases and repositories.
 */
public interface SubjectClassService {

    /**
     * Retrieves all subjects associated with a specific class, including the subject-class association ID.
     * Validates that the class belongs to the authenticated teacher.
     *
     * @param classId The unique identifier of the class
     * @return List of subject-class details belonging to the specified class
     */
    List<SubjectClassDetail> getSubjectsByClassId(Integer classId);

    /**
     * Retrieves all classes with their associated subjects for the authenticated teacher.
     *
     * @return List of classes with their subjects
     */
    List<ClassWithSubjects> getAllClassesWithSubjects();

    /**
     * Assigns multiple subjects to a class.
     * Validates that the class and all subjects belong to the authenticated teacher.
     * Validates that no duplicate associations exist.
     *
     * @param classId    The unique identifier of the class
     * @param subjectIds The list of subject IDs to assign to the class
     * @return List of subject-class details that were assigned to the class
     */
    List<SubjectClassDetail> assignSubjectsToClass(Integer classId, List<Integer> subjectIds);

    /**
     * Removes multiple subjects from a class by soft-deleting the associations.
     * Validates that the class belongs to the authenticated teacher.
     *
     * @param classId    The unique identifier of the class
     * @param subjectIds The list of subject IDs to remove from the class
     */
    void removeSubjectsFromClass(Integer classId, List<Integer> subjectIds);
}

