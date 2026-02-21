package org.web.codefm.domain.repository.teachernotebook;

import org.web.codefm.domain.entity.teachernotebook.Subject;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for subject data access operations.
 * Provides methods to retrieve and manage subject information.
 */
public interface SubjectRepository {

    /**
     * Finds all subjects associated with a specific teacher.
     *
     * @param teacherId The unique identifier of the teacher
     * @return List of subjects belonging to the specified teacher
     */
    List<Subject> findByTeacherId(Integer teacherId);

    /**
     * Saves a new subject or updates an existing one.
     *
     * @param subject The subject object to save.
     * @return The saved subject object, typically with the generated ID.
     */
    Subject save(Subject subject);

    /**
     * Finds a subject by its ID.
     *
     * @param subjectId The unique identifier of the subject.
     * @return An Optional containing the Subject if found, or empty otherwise.
     */
    Optional<Subject> findById(Integer subjectId);

    /**
     * Finds a subject by its ID and teacher ID.
     *
     * @param subjectId The unique identifier of the subject.
     * @param teacherId The unique identifier of the teacher.
     * @return An Optional containing the Subject if found and owned by the teacher, or empty otherwise.
     */
    Optional<Subject> findByIdAndTeacherId(Integer subjectId, Integer teacherId);

    /**
     * Soft-deletes a subject by setting its deletion date.
     *
     * @param subjectId The ID of the subject to soft-delete.
     * @param teacherId The ID of the teacher attempting the soft-delete.
     * @return The soft-deleted Subject object.
     */
    Subject softDeleteSubject(Integer subjectId, Integer teacherId);
}
