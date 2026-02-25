package org.web.codefm.domain.usecase.teachernotebook;

import org.web.codefm.domain.entity.teachernotebook.ClassWithSubjects;
import org.web.codefm.domain.entity.teachernotebook.SubjectClassDetail;

import java.util.List;

/**
 * Interface that defines subject-class association operations for teachers.
 * Handles subject assignment and removal from classes.
 */
public interface SubjectClassUseCase {

    /**
     * Retrieves all subjects associated with a specific class, including the subject-class association ID.
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
     *
     * @param classId    The unique identifier of the class
     * @param subjectIds The list of subject IDs to assign to the class
     * @return List of subject-class details that were assigned to the class
     */
    List<SubjectClassDetail> assignSubjectsToClass(Integer classId, List<Integer> subjectIds);

    /**
     * Removes multiple subjects from a class.
     *
     * @param classId    The unique identifier of the class
     * @param subjectIds The list of subject IDs to remove from the class
     */
    void removeSubjectsFromClass(Integer classId, List<Integer> subjectIds);
}

