package org.web.codefm.domain.service.teachernotebook;

import org.web.codefm.domain.entity.teachernotebook.School;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for school business logic operations.
 * Acts as an intermediary between use cases and repositories.
 */
public interface SchoolService {

    /**
     * Retrieves all schools associated with a specific teacher.
     *
     * @param teacherId The unique identifier of the teacher
     * @return List of schools belonging to the specified teacher
     */
    List<School> getSchoolsByTeacherId(Integer teacherId);

    /**
     * Creates a new school.
     *
     * @param school         The school object to create.
     * @param acceptLanguage The Accept-Language header for error messages.
     * @return The created school object.
     */
    School createSchool(School school, String acceptLanguage);

    /**
     * Soft-deletes a school by setting its deletion date.
     *
     * @param schoolId       The ID of the school to soft-delete.
     * @param teacherId      The ID of the teacher attempting the soft-delete.
     * @param acceptLanguage The Accept-Language header for error messages.
     */
    void softDeleteSchool(Integer schoolId, Integer teacherId, String acceptLanguage);

    /**
     * Retrieves a school by its ID.
     *
     * @param schoolId The unique identifier of the school.
     * @return An Optional containing the School if found, or empty otherwise.
     */
    Optional<School> getSchoolById(Integer schoolId);

    /**
     * Updates an existing school.
     *
     * @param schoolId       The ID of the school to update.
     * @param school         The school object containing the updated data.
     * @param teacherId      The ID of the teacher attempting the update.
     * @param acceptLanguage The Accept-Language header for error messages.
     * @return The updated school object.
     */
    School updateSchool(Integer schoolId, School school, Integer teacherId, String acceptLanguage);
}
