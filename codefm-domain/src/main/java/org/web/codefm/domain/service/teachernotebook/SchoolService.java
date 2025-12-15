package org.web.codefm.domain.service.teachernotebook;

import org.web.codefm.domain.entity.teachernotebook.School;

import java.util.List;

public interface SchoolService {

    List<School> getSchoolsByTeacherId(Integer teacherId);

    School createSchool(School school, String acceptLanguage); // Added acceptLanguage parameter
}
