package org.web.codefm.usecase.teachernotebook;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web.codefm.domain.entity.teachernotebook.School;
import org.web.codefm.domain.service.teachernotebook.SchoolService;
import org.web.codefm.domain.session.SessionParameter;
import org.web.codefm.domain.session.SessionUser;
import org.web.codefm.domain.usecase.teachernotebook.SchoolUseCase;
import org.web.codefm.domain.util.SchoolYearUtil;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchoolUseCaseImpl implements SchoolUseCase {

    private final SchoolService schoolService;
    private final SessionUser sessionUser;

    @Override
    public List<School> getSchoolsByTeacher() {
      Integer teacherId = sessionUser.getParameter(SessionParameter.TEACHER_ID, Integer.class);
        List<School> schools = schoolService.getSchoolsByTeacherId(teacherId);

        schools.forEach(school ->
                Optional.ofNullable(school.getClasses())
                        .ifPresent(classes -> classes.sort(Comparator.comparing(
                                SchoolYearUtil::parseSchoolYear, Comparator.reverseOrder()))));

        schools.sort(Comparator.comparing(
                this::getMaxSchoolYearForSchool, Comparator.reverseOrder()));

        return schools;
    }

    @Override
    public School createSchool(School school) {
      Integer teacherId = sessionUser.getParameter(SessionParameter.TEACHER_ID, Integer.class);
        school.setTeacherId(teacherId);
        return schoolService.createSchool(school);
    }

    @Override
    public void softDeleteSchool(Integer schoolId) {
      Integer teacherId = sessionUser.getParameter(SessionParameter.TEACHER_ID, Integer.class);
        schoolService.softDeleteSchool(schoolId, teacherId);
    }

    @Override
    public School updateSchool(Integer schoolId, School school) {
      Integer teacherId = sessionUser.getParameter(SessionParameter.TEACHER_ID, Integer.class);
        return schoolService.updateSchool(schoolId, school, teacherId);
    }

    /**
     * Retrieves the highest (most recent) schoolYear from a school's classes.
     * If the school has no classes or all have invalid formats, it returns 0.
     *
     * @param school The school to evaluate.
     * @return The highest schoolYear as an Integer, or 0 if no valid classes are found.
     */
    private Integer getMaxSchoolYearForSchool(School school) {
        return Optional.ofNullable(school.getClasses())
                .orElse(Collections.emptyList())
                .stream()
                .map(SchoolYearUtil::parseSchoolYear)
                .max(Comparator.naturalOrder())
                .orElse(0);
    }
}
