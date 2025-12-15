package org.web.codefm.domain.usecase.teachernotebook;

import org.web.codefm.domain.entity.teachernotebook.School;

import java.util.List;

/**
 * Interface that defines school operations for teachers.
 * Handles school data retrieval and management.
 */
public interface SchoolUseCase {

    /**
     * Retrieves all schools associated with the authenticated teacher.
     *
     * @return List of schools belonging to the teacher
     */
    List<School> getSchoolsByTeacher();

    /**
     * Creates a new school based on the provided data and assigns it to the authenticated teacher.
     *
     * @param school         The school object containing the data for the new school.
     * @param acceptLanguage The language accepted by the client for error messages.
     * @return The created school object.
     */
    School createSchool(School school, String acceptLanguage);

    /**
     * Soft-deletes a school by setting its deletion date.
     *
     * @param schoolId       The ID of the school to soft-delete.
     * @param acceptLanguage The Accept-Language header for error messages.
     */
    void softDeleteSchool(Integer schoolId, String acceptLanguage);
}
