package org.web.codefm.domain.usecase.teachernotebook;

import org.web.codefm.domain.entity.teachernotebook.Class;

import java.util.List;

/**
 * Interface that defines class operations for teachers.
 * Handles class data retrieval for a specific school.
 */
public interface ClassUseCase {

    /**
     * Retrieves all active classes for a specific school owned by the authenticated teacher.
     *
     * @param schoolId The unique identifier of the school
     * @return List of active classes belonging to the specified school
     */
    List<Class> getClassesBySchoolId(Integer schoolId);
}

