package org.web.codefm.domain.repository.teachernotebook;

import org.web.codefm.domain.entity.School;

import java.util.List;

/**
 * Repository interface for school data access operations.
 * Provides methods to retrieve and manage school information.
 */
public interface SchoolRepository {

    /**
     * Finds all schools associated with a specific teacher.
     *
     * @param teacherId The unique identifier of the teacher
     * @return List of schools belonging to the specified teacher
     */
    List<School> findByTeacherId(Integer teacherId);
}
