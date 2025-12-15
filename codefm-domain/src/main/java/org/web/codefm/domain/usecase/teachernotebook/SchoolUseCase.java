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
}
