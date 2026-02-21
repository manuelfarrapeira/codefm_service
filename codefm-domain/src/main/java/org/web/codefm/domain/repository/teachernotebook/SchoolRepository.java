package org.web.codefm.domain.repository.teachernotebook;

import org.web.codefm.domain.entity.teachernotebook.School;

import java.util.List;
import java.util.Optional;

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

    /**
     * Saves a new school or updates an existing one.
     *
     * @param school The school object to save.
     * @return The saved school object, typically with the generated ID.
     */
    School save(School school);

    /**
     * Finds a school by its ID.
     *
     * @param schoolId The unique identifier of the school.
     * @return An Optional containing the School if found, or empty otherwise.
     */
    Optional<School> findById(Integer schoolId);

    /**
     * Soft-deletes a school by setting its deletion date.
     *
     * @param schoolId  The ID of the school to soft-delete.
     * @param teacherId The ID of the teacher attempting the soft-delete.
     * @return The soft-deleted School object.
     */
    School softDeleteSchool(Integer schoolId, Integer teacherId);
}
