package org.web.codefm.domain.service.teachernotebook;

import org.web.codefm.domain.entity.teachernotebook.Class;

import java.util.List;

/**
 * Service interface for class business logic operations.
 * Acts as an intermediary between use cases and repositories.
 */
public interface ClassService {

    /**
     * Retrieves all active classes for a specific school, verifying teacher ownership.
     *
     * @param schoolId  The unique identifier of the school
     * @param teacherId The unique identifier of the teacher
     * @return List of active classes belonging to the specified school
     */
    List<Class> getActiveClassesBySchoolIdAndTeacherId(Integer schoolId, Integer teacherId);
}

