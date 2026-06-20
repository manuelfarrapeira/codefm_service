package org.web.codefm.usecase.teachernotebook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web.codefm.domain.entity.teachernotebook.Class;
import org.web.codefm.domain.service.teachernotebook.CascadeSoftDeleteService;
import org.web.codefm.domain.service.teachernotebook.ClassService;
import org.web.codefm.domain.session.SessionParameter;
import org.web.codefm.domain.session.SessionUser;
import org.web.codefm.domain.usecase.teachernotebook.ClassUseCase;
import org.web.codefm.domain.util.SchoolYearUtil;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClassUseCaseImpl implements ClassUseCase {

    private final ClassService classService;
    private final CascadeSoftDeleteService cascadeSoftDeleteService;
    private final SessionUser sessionUser;

    @Override
    public List<Class> getClassesBySchoolId(Integer schoolId) {
        Integer teacherId = sessionUser.getParameter(SessionParameter.TEACHER_ID);

        List<Class> classes = classService.getActiveClassesBySchoolIdAndTeacherId(schoolId, teacherId);

        classes.sort(Comparator.comparing(SchoolYearUtil::parseSchoolYear, Comparator.reverseOrder()));

        return classes;
    }

    @Override
    public Class createClass(Class clazz) {
        Integer teacherId = sessionUser.getParameter(SessionParameter.TEACHER_ID);
        return classService.createClass(clazz, teacherId);
    }

    @Override
    @Transactional
    public void softDeleteClass(Integer classId) {
        Integer teacherId = sessionUser.getParameter(SessionParameter.TEACHER_ID);
        cascadeSoftDeleteService.cascadeDeleteChildrenOfClass(classId);
        classService.softDeleteClass(classId, teacherId);
    }

  @Override
  public Class updateClass(Integer classId, Class clazz) {
      Integer teacherId = sessionUser.getParameter(SessionParameter.TEACHER_ID);
    return classService.updateClass(classId, clazz, teacherId);
  }
}

