package org.web.codefm.domain.repository.teachernotebook;

import org.web.codefm.domain.entity.teachernotebook.Class;

import java.util.List;

/**
 * Repository interface for class data access operations.
 * Provides methods to retrieve and manage class information.
 */
public interface ClassRepository {

    /**
     * Finds all active classes (not soft-deleted) for a specific school.
     *
     * @param schoolId  The unique identifier of the school
     * @param teacherId The unique identifier of the teacher (to verify ownership)
     * @return List of active classes belonging to the specified school
     */
    List<Class> findActiveClassesBySchoolIdAndTeacherId(Integer schoolId, Integer teacherId);
}

