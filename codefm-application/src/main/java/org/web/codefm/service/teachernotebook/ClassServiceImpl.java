package org.web.codefm.service.teachernotebook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.web.codefm.domain.entity.teachernotebook.Class;
import org.web.codefm.domain.entity.teachernotebook.School;
import org.web.codefm.domain.exception.teachernotebook.SchoolForbiddenException;
import org.web.codefm.domain.exception.teachernotebook.SchoolNotFoundException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.repository.teachernotebook.ClassRepository;
import org.web.codefm.domain.service.teachernotebook.ClassService;
import org.web.codefm.domain.service.teachernotebook.SchoolService;
import org.web.codefm.domain.session.SessionUser;

import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClassServiceImpl implements ClassService {

    private final ClassRepository classRepository;
    private final SchoolService schoolService;
    private final MessageSource messageSource;
    private final SessionUser sessionUser;

    @Override
    public List<Class> getActiveClassesBySchoolIdAndTeacherId(Integer schoolId, Integer teacherId) {
        Locale locale = sessionUser.getLocale();

        School school = schoolService.getSchoolById(schoolId)
                .orElseThrow(() -> new SchoolNotFoundException(
                        messageSource.getMessage(MessageKeys.SCHOOL_NOT_FOUND, null, locale)));

        if (!school.getTeacherId().equals(teacherId)) {
            throw new SchoolForbiddenException(
                    messageSource.getMessage(MessageKeys.SCHOOL_FORBIDDEN, null, locale));
        }

        return classRepository.findActiveClassesBySchoolIdAndTeacherId(schoolId, teacherId);
    }
}

