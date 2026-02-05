package org.web.codefm.domain.service.teachernotebook;

import org.web.codefm.domain.entity.teachernotebook.Subject;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for subject business logic operations.
 * Acts as an intermediary between use cases and repositories.
 */
public interface SubjectService {

    /**
     * Retrieves all subjects associated with the authenticated teacher.
     *
     * @return List of subjects belonging to the authenticated teacher
     */
    List<Subject> getSubjectsByTeacher();

    /**
     * Creates a new subject for the authenticated teacher.
     *
     * @param subject The subject object to create.
     * @return The created subject object.
     */
    Subject createSubject(Subject subject);

    /**
     * Retrieves a subject by its ID.
     *
     * @param subjectId The unique identifier of the subject.
     * @return An Optional containing the Subject if found, or empty otherwise.
     */
    Optional<Subject> getSubjectById(Integer subjectId);

    /**
     * Soft-deletes a subject by setting its deletion date.
     *
     * @param subjectId The ID of the subject to soft-delete.
     */
    void softDeleteSubject(Integer subjectId);

    /**
     * Updates an existing subject.
     *
     * @param subjectId The ID of the subject to update.
     * @param subject   The subject object containing the updated data.
     * @return The updated subject object.
     */
    Subject updateSubject(Integer subjectId, Subject subject);
}
