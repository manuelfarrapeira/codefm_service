package org.web.codefm.usecase.teachernotebook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web.codefm.domain.entity.School;
import org.web.codefm.domain.service.teachernotebook.SchoolService;
import org.web.codefm.domain.session.SessionParameter;
import org.web.codefm.domain.session.SessionUser;
import org.web.codefm.domain.usecase.teachernotebook.SchoolUseCase;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchoolUseCaseImpl implements SchoolUseCase {

    private final SchoolService schoolService;
    private final SessionUser sessionUser;

    @Override
    public List<School> getSchoolsByTeacher() {
        Integer teacherId = Integer.valueOf(sessionUser.getParameters().get(SessionParameter.TEACHER_ID.getClaimName()));
        return schoolService.getSchoolsByTeacherId(teacherId);
    }
}
