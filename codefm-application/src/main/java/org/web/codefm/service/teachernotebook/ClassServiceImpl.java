package org.web.codefm.service.teachernotebook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web.codefm.domain.entity.exception.ErrorMessage;
import org.web.codefm.domain.entity.teachernotebook.Class;
import org.web.codefm.domain.exception.teachernotebook.ClassValidationException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.repository.teachernotebook.*;
import org.web.codefm.domain.service.teachernotebook.ClassService;
import org.web.codefm.domain.service.teachernotebook.SchoolService;
import org.web.codefm.domain.session.SessionUser;
import org.web.codefm.util.ClassValidationUtil;
import org.web.codefm.util.SchoolValidationUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClassServiceImpl implements ClassService {

    private static final Pattern SCHOOL_YEAR_PATTERN = Pattern.compile("^\\d{2}/\\d{2}$");
    private static final String FIELD_SCHOOL_YEAR = "schoolYear";

    private final ClassRepository classRepository;
    private final SchoolService schoolService;
    private final SubjectClassRepository subjectClassRepository;
    private final ScheduleRepository scheduleRepository;
    private final StudentClassRepository studentClassRepository;
    private final ExerciseRepository exerciseRepository;
    private final MessageSource messageSource;
    private final SessionUser sessionUser;

    @Override
    public List<Class> getActiveClassesBySchoolIdAndTeacherId(Integer schoolId, Integer teacherId) {
        Locale locale = sessionUser.getLocale();
        SchoolValidationUtil.validateSchoolOwnership(schoolId, teacherId, schoolService, messageSource, locale);
        return classRepository.findActiveClassesBySchoolIdAndTeacherId(schoolId, teacherId);
    }

    @Override
    public Class createClass(Class clazz, Integer teacherId) {
        Locale locale = sessionUser.getLocale();
        List<ErrorMessage> errors = new ArrayList<>();

        SchoolValidationUtil.validateSchoolOwnership(clazz.getSchoolId(), teacherId, schoolService, messageSource, locale);

        validateClass(clazz, errors, locale);

        if (!errors.isEmpty()) {
            throw new ClassValidationException(errors);
        }

        return classRepository.save(clazz);
    }

    @Override
    public Optional<Class> getClassById(Integer classId) {
        return classRepository.findById(classId);
    }

    @Override
    @Transactional
    public void softDeleteClass(Integer classId, Integer teacherId) {
        Locale locale = sessionUser.getLocale();
        ClassValidationUtil.validateClassOwnership(classId, teacherId, this, schoolService, messageSource, locale);

        List<Integer> subjectClassIds = subjectClassRepository.findActiveIdsByClassId(classId);

        if (!subjectClassIds.isEmpty()) {
            exerciseRepository.softDeleteBySubjectClassIds(subjectClassIds);
        }

        studentClassRepository.softDeleteByClassId(classId);
        subjectClassRepository.softDeleteByClassId(classId);
        scheduleRepository.softDeleteByClassId(classId);

        classRepository.softDeleteClass(classId, teacherId);
    }

  @Override
  @Transactional
  public Class updateClass(Integer classId, Class clazz, Integer teacherId) {
    Locale locale = sessionUser.getLocale();
    List<ErrorMessage> errors = new ArrayList<>();

    validateClass(clazz, errors, locale);

    if (!errors.isEmpty()) {
      throw new ClassValidationException(errors);
    }

    Class existingClass = ClassValidationUtil.validateClassOwnership(
        classId, teacherId, this, schoolService, messageSource, locale);

    existingClass.setName(clazz.getName());
    existingClass.setSchoolYear(clazz.getSchoolYear());

    return classRepository.save(existingClass);
  }

    private void validateClass(Class clazz, List<ErrorMessage> errors, Locale locale) {
        if (clazz.getName() == null || clazz.getName().trim().isEmpty()) {
            errors.add(new ErrorMessage("name", messageSource.getMessage(MessageKeys.CLASS_VALIDATION_NAME_REQUIRED, null, locale)));
        }

        if (clazz.getSchoolYear() == null || clazz.getSchoolYear().trim().isEmpty()) {
            errors.add(new ErrorMessage(FIELD_SCHOOL_YEAR, messageSource.getMessage(MessageKeys.CLASS_VALIDATION_SCHOOL_YEAR_REQUIRED, null, locale)));
        } else if (!SCHOOL_YEAR_PATTERN.matcher(clazz.getSchoolYear()).matches()) {
            errors.add(new ErrorMessage(FIELD_SCHOOL_YEAR, messageSource.getMessage(MessageKeys.CLASS_VALIDATION_SCHOOL_YEAR_FORMAT_INVALID, null, locale)));
        } else {
            String[] years = clazz.getSchoolYear().split("/");
            try {
                int firstYear = Integer.parseInt(years[0]);
                int secondYear = Integer.parseInt(years[1]);
                if (secondYear != firstYear + 1) {
                    errors.add(new ErrorMessage(FIELD_SCHOOL_YEAR, messageSource.getMessage(MessageKeys.CLASS_VALIDATION_SCHOOL_YEAR_NOT_CONSECUTIVE, null, locale)));
                }
            } catch (NumberFormatException e) {
                log.warn("Failed to parse schoolYear: {}", clazz.getSchoolYear(), e);
                errors.add(new ErrorMessage(FIELD_SCHOOL_YEAR, messageSource.getMessage(MessageKeys.CLASS_VALIDATION_SCHOOL_YEAR_FORMAT_INVALID, null, locale)));
            }
        }
    }
}

