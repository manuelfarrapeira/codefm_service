package org.web.codefm.domain.repository.teachernotebook;

import org.web.codefm.domain.entity.teachernotebook.ClassWithSubjects;
import org.web.codefm.domain.entity.teachernotebook.Subject;
import org.web.codefm.domain.entity.teachernotebook.SubjectClass;

import java.util.List;

/**
 * Repository interface for subject-class association data access operations.
 * Provides methods to manage the relationship between subjects and classes.
 */
public interface SubjectClassRepository {

    /**
     * Finds all subjects associated with a specific class.
     *
     * @param classId The unique identifier of the class
     * @return List of subjects belonging to the specified class
     */
    List<Subject> findSubjectsByClassId(Integer classId);

    /**
     * Saves multiple subject-class associations.
     *
     * @param subjectClasses The list of subject-class associations to save
     * @return The list of saved subject-class associations
     */
    List<SubjectClass> saveAll(List<SubjectClass> subjectClasses);

    /**
     * Soft-deletes multiple subject-class associations by setting their deletion date.
     *
     * @param classId    The unique identifier of the class
     * @param subjectIds The list of subject IDs to remove from the class
     */
    void softDeleteAll(Integer classId, List<Integer> subjectIds);

    /**
     * Checks if a subject-class association already exists and is active.
     *
     * @param subjectId The unique identifier of the subject
     * @param classId   The unique identifier of the class
     * @return true if the association exists and is not soft-deleted, false otherwise
     */
    boolean existsBySubjectIdAndClassIdAndDeletionDateIsNull(Integer subjectId, Integer classId);

    /**
     * Finds all classes with their associated subjects for a specific teacher.
     *
     * @param teacherId The unique identifier of the teacher
     * @return List of classes with their subjects belonging to the specified teacher
     */
    List<ClassWithSubjects> findAllClassesWithSubjectsByTeacherId(Integer teacherId);
}

