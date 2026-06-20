package org.web.codefm.domain.repository.teachernotebook;

import org.web.codefm.domain.entity.teachernotebook.ClassWithSubjects;
import org.web.codefm.domain.entity.teachernotebook.SubjectClass;
import org.web.codefm.domain.entity.teachernotebook.SubjectClassDetail;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for subject-class association data access operations.
 * Provides methods to manage the relationship between subjects and classes.
 */
public interface SubjectClassRepository {

    /**
     * Finds all subjects associated with a specific class, including the subject-class association ID.
     *
     * @param classId The unique identifier of the class
     * @return List of subject-class details belonging to the specified class
     */
    List<SubjectClassDetail> findSubjectsByClassId(Integer classId);

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

    /**
     * Soft-deletes all subject-class associations for a specific class.
     *
     * @param classId The unique identifier of the class
     */
    void softDeleteByClassId(Integer classId);

    /**
     * Soft-deletes all subject-class associations for a specific subject.
     *
     * @param subjectId The unique identifier of the subject
     */
    void softDeleteBySubjectId(Integer subjectId);

    /**
     * Finds all active subject-class association IDs for a specific class.
     *
     * @param classId The unique identifier of the class
     * @return List of active subject-class association IDs
     */
    List<Integer> findActiveIdsByClassId(Integer classId);

    /**
     * Finds all active subject-class association IDs for a specific subject.
     *
     * @param subjectId The unique identifier of the subject
     * @return List of active subject-class association IDs
     */
    List<Integer> findActiveIdsBySubjectId(Integer subjectId);

    /**
     * Finds the ID of an active subject-class association.
     *
     * @param subjectId The unique identifier of the subject
     * @param classId   The unique identifier of the class
     * @return Optional with the subject-class association ID if found and active
     */
    Optional<Integer> findIdBySubjectIdAndClassId(Integer subjectId, Integer classId);

    /**
     * Finds a subject-class association by its ID.
     *
     * @param id The unique identifier of the subject-class association
     * @return Optional containing the subject-class association if found
     */
    Optional<SubjectClass> findById(Integer id);
}
