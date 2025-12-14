package org.web.codefm.domain.service.teachernotebook;

import org.web.codefm.domain.entity.School;

import java.util.List;

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
}
