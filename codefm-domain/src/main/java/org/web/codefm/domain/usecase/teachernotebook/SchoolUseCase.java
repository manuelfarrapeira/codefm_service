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
     * @return The created school object.
     */
    School createSchool(School school);

    /**
     * Soft-deletes a school by setting its deletion date.
     *
     * @param schoolId       The ID of the school to soft-delete.
     */
    void softDeleteSchool(Integer schoolId);

    /**
     * Updates an existing school based on the provided data.
     *
     * @param schoolId       The ID of the school to update.
     * @param school         The school object containing the updated data.
     * @return The updated school object.
     */
    School updateSchool(Integer schoolId, School school);
}
