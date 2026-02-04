package org.web.codefm.domain.usecase.teachernotebook;

import org.web.codefm.domain.entity.teachernotebook.Subject;

import java.util.List;

/**
 * Interface that defines subject operations for teachers.
 * Handles subject data retrieval and management.
 */
public interface SubjectUseCase {

    /**
     * Retrieves all subjects associated with the authenticated teacher.
     *
     * @return List of subjects belonging to the teacher
     */
    List<Subject> getSubjectsByTeacher();

    /**
     * Creates a new subject based on the provided data and assigns it to the authenticated teacher.
     *
     * @param subject The subject object containing the data for the new subject.
     * @return The created subject object.
     */
    Subject createSubject(Subject subject);

    /**
     * Soft-deletes a subject by setting its deletion date.
     *
     * @param subjectId The ID of the subject to soft-delete.
     */
    void softDeleteSubject(Integer subjectId);

    /**
     * Updates an existing subject based on the provided data.
     *
     * @param subjectId The ID of the subject to update.
     * @param subject   The subject object containing the updated data.
     * @return The updated subject object.
     */
    Subject updateSubject(Integer subjectId, Subject subject);
}
