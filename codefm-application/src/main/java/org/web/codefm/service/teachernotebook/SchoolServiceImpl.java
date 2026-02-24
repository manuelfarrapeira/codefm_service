package org.web.codefm.service.teachernotebook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web.codefm.domain.entity.exception.ErrorMessage;
import org.web.codefm.domain.entity.teachernotebook.School;
import org.web.codefm.domain.exception.teachernotebook.SchoolValidationException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.repository.teachernotebook.*;
import org.web.codefm.domain.service.teachernotebook.ExerciseDocumentService;
import org.web.codefm.domain.service.teachernotebook.SchoolService;
import org.web.codefm.domain.session.SessionUser;
import org.web.codefm.util.SchoolValidationUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchoolServiceImpl implements SchoolService {

    private final SchoolRepository schoolRepository;
    private final ClassRepository classRepository;
    private final SubjectClassRepository subjectClassRepository;
    private final ScheduleRepository scheduleRepository;
    private final StudentClassRepository studentClassRepository;
    private final ExerciseRepository exerciseRepository;
    private final ExerciseStudentGradeRepository exerciseStudentGradeRepository;
    private final ExerciseDocumentService exerciseDocumentService;
    private final MessageSource messageSource;
    private final SessionUser sessionUser;

    @Override
    public List<School> getSchoolsByTeacherId(Integer teacherId) {
        return schoolRepository.findByTeacherId(teacherId);
    }

    @Override
    public School createSchool(School school) {
        List<ErrorMessage> errors = new ArrayList<>();
        Locale locale = sessionUser.getLocale();

        validateSchool(school, errors, locale);

        if (!errors.isEmpty()) {
            throw new SchoolValidationException(errors);
        }

        return schoolRepository.save(school);
    }

    @Override
    @Transactional
    public void softDeleteSchool(Integer schoolId, Integer teacherId) {
        Locale locale = sessionUser.getLocale();
        SchoolValidationUtil.validateSchoolOwnership(schoolId, teacherId, this, messageSource, locale);

        List<Integer> classIds = classRepository.findActiveIdsBySchoolId(schoolId);

        for (Integer classId : classIds) {
            cascadeDeleteClass(classId);
        }

        classRepository.softDeleteBySchoolId(schoolId);
        schoolRepository.softDeleteSchool(schoolId, teacherId);
    }

    private void cascadeDeleteClass(Integer classId) {
        List<Integer> subjectClassIds = subjectClassRepository.findActiveIdsByClassId(classId);

        if (!subjectClassIds.isEmpty()) {
            List<Integer> exerciseIds = exerciseRepository.findActiveIdsBySubjectClassIds(subjectClassIds);
            if (!exerciseIds.isEmpty()) {
                exerciseStudentGradeRepository.softDeleteByExerciseIds(exerciseIds);
                exerciseDocumentService.deleteDocumentsByExerciseIds(exerciseIds);
            }
            exerciseRepository.softDeleteBySubjectClassIds(subjectClassIds);
        }

        studentClassRepository.softDeleteByClassId(classId);
        subjectClassRepository.softDeleteByClassId(classId);
        scheduleRepository.softDeleteByClassId(classId);
    }

    @Override
    @Transactional
    public School updateSchool(Integer schoolId, School school, Integer teacherId) {
        Locale locale = sessionUser.getLocale();
        List<ErrorMessage> errors = new ArrayList<>();

        validateSchool(school, errors, locale);

        if (!errors.isEmpty()) {
            throw new SchoolValidationException(errors);
        }

        School existingSchool = SchoolValidationUtil.validateSchoolOwnership(schoolId, teacherId, this, messageSource, locale);

        existingSchool.setName(school.getName());
        existingSchool.setTown(school.getTown());
        existingSchool.setTlf(school.getTlf());

        return schoolRepository.save(existingSchool);
    }

    @Override
    public Optional<School> getSchoolById(Integer schoolId) {
        return schoolRepository.findById(schoolId);
    }

    private void validateSchool(School school, List<ErrorMessage> errors, Locale locale) {
        if (school.getName() == null || school.getName().trim().isEmpty()) {
            String translatedMessage = messageSource.getMessage(MessageKeys.SCHOOL_VALIDATION_NAME_REQUIRED, null, locale);
            errors.add(new ErrorMessage("name", translatedMessage));
        }

        if (school.getTlf() != null && String.valueOf(school.getTlf()).length() != 9) {
            String translatedMessage = messageSource.getMessage(MessageKeys.SCHOOL_VALIDATION_TLF_INVALID, null, locale);
            errors.add(new ErrorMessage("tlf", translatedMessage));
        }
    }
}
