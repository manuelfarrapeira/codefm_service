package org.web.codefm.service.teachernotebook;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.web.codefm.domain.entity.exception.ErrorMessage;
import org.web.codefm.domain.entity.teachernotebook.Class;
import org.web.codefm.domain.entity.teachernotebook.Exercise;
import org.web.codefm.domain.exception.teachernotebook.ClassForbiddenException;
import org.web.codefm.domain.exception.teachernotebook.ClassNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.ExerciseNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.ExerciseValidationException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.repository.teachernotebook.ClassRepository;
import org.web.codefm.domain.repository.teachernotebook.ExerciseRepository;
import org.web.codefm.domain.repository.teachernotebook.SubjectClassRepository;
import org.web.codefm.domain.service.teachernotebook.ExerciseService;
import org.web.codefm.domain.session.SessionParameter;
import org.web.codefm.domain.session.SessionUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ExerciseServiceImpl implements ExerciseService {

    private static final String FIELD_PERCENTAGE_GRADE = "percentageGrade";

    private final ExerciseRepository exerciseRepository;
    private final ClassRepository classRepository;
    private final SubjectClassRepository subjectClassRepository;
    private final MessageSource messageSource;
    private final SessionUser sessionUser;

    @Override
    public List<Exercise> getExercisesByClassId(Integer classId) {
        Integer teacherId = getTeacherId();
        Locale locale = sessionUser.getLocale();

        classRepository.findById(classId)
                .orElseThrow(() -> new ClassNotFoundException(
                        messageSource.getMessage(MessageKeys.CLASS_NOT_FOUND, null, locale)
                ));

        Class cl = classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId)
                .orElseThrow(() -> new ClassForbiddenException(
                        messageSource.getMessage(MessageKeys.CLASS_FORBIDDEN, null, locale)
                ));

        return exerciseRepository.findByClassId(cl.getId());
    }

    @Override
    public Exercise createExercise(Integer subjectClassId, Exercise exercise) {
        Integer teacherId = getTeacherId();
        List<ErrorMessage> errors = new ArrayList<>();

        validateSubjectClassOwnership(subjectClassId, teacherId);
        validateExercise(exercise, errors);
        validatePercentageGradeSum(subjectClassId, exercise.getQuarter(), exercise.getPercentageGrade(), null, errors);

        if (!errors.isEmpty()) {
            throw new ExerciseValidationException(errors);
        }

        Exercise exerciseToSave = Exercise.builder()
                .subjectClassId(subjectClassId)
                .title(exercise.getTitle())
                .description(exercise.getDescription())
                .quarter(exercise.getQuarter())
                .percentageGrade(exercise.getPercentageGrade())
                .maxGrade(exercise.getMaxGrade())
                .build();

        return exerciseRepository.save(exerciseToSave);
    }

    @Override
    public Exercise updateExercise(Integer id, Exercise exercise) {
        Integer teacherId = getTeacherId();
        List<ErrorMessage> errors = new ArrayList<>();

        Exercise existingExercise = exerciseRepository.findByIdAndTeacherId(id, teacherId)
                .orElseThrow(() -> new ExerciseNotFoundException(
                        messageSource.getMessage(MessageKeys.EXERCISE_NOT_FOUND, null, sessionUser.getLocale())
                ));

        validateExercise(exercise, errors);
        validatePercentageGradeSum(existingExercise.getSubjectClassId(), exercise.getQuarter(), exercise.getPercentageGrade(), existingExercise.getId(), errors);

        if (!errors.isEmpty()) {
            throw new ExerciseValidationException(errors);
        }

        existingExercise.setTitle(exercise.getTitle());
        existingExercise.setDescription(exercise.getDescription());
        existingExercise.setQuarter(exercise.getQuarter());
        existingExercise.setPercentageGrade(exercise.getPercentageGrade());
        existingExercise.setMaxGrade(exercise.getMaxGrade());

        return exerciseRepository.update(existingExercise);
    }

    @Override
    public void deleteExercise(Integer id) {
        Integer teacherId = getTeacherId();

        Exercise exercise = exerciseRepository.findByIdAndTeacherId(id, teacherId)
                .orElseThrow(() -> new ExerciseNotFoundException(
                        messageSource.getMessage(MessageKeys.EXERCISE_NOT_FOUND, null, sessionUser.getLocale())
                ));

        exerciseRepository.softDelete(exercise.getId());
    }

    private void validateSubjectClassOwnership(Integer subjectClassId, Integer teacherId) {
        subjectClassRepository.findById(subjectClassId)
                .orElseThrow(() -> new ExerciseNotFoundException(
                        messageSource.getMessage(MessageKeys.EXERCISE_VALIDATION_SUBJECT_CLASS_NOT_FOUND, null, sessionUser.getLocale())
                ));

        if (!exerciseRepository.subjectClassBelongsToTeacher(subjectClassId, teacherId)) {
            throw new ClassForbiddenException(
                    messageSource.getMessage(MessageKeys.CLASS_FORBIDDEN, null, sessionUser.getLocale())
            );
        }
    }

    private void validateExercise(Exercise exercise, List<ErrorMessage> errors) {
        if (exercise.getTitle() == null || exercise.getTitle().trim().isEmpty()) {
            String message = messageSource.getMessage(MessageKeys.EXERCISE_VALIDATION_TITLE_REQUIRED, null, sessionUser.getLocale());
            errors.add(new ErrorMessage("title", message));
        }

        if (exercise.getQuarter() == null) {
            String message = messageSource.getMessage(MessageKeys.EXERCISE_VALIDATION_QUARTER_REQUIRED, null, sessionUser.getLocale());
            errors.add(new ErrorMessage("quarter", message));
        } else if (exercise.getQuarter() < 1 || exercise.getQuarter() > 3) {
            String message = messageSource.getMessage(MessageKeys.EXERCISE_VALIDATION_QUARTER_INVALID, null, sessionUser.getLocale());
            errors.add(new ErrorMessage("quarter", message));
        }

        if (exercise.getPercentageGrade() == null) {
            String message = messageSource.getMessage(MessageKeys.EXERCISE_VALIDATION_PERCENTAGE_GRADE_REQUIRED, null, sessionUser.getLocale());
            errors.add(new ErrorMessage(FIELD_PERCENTAGE_GRADE, message));
        } else if (exercise.getPercentageGrade() < 1 || exercise.getPercentageGrade() > 100) {
            String message = messageSource.getMessage(MessageKeys.EXERCISE_VALIDATION_PERCENTAGE_GRADE_INVALID, null, sessionUser.getLocale());
            errors.add(new ErrorMessage(FIELD_PERCENTAGE_GRADE, message));
        }

        if (exercise.getMaxGrade() == null) {
            String message = messageSource.getMessage(MessageKeys.EXERCISE_VALIDATION_MAX_GRADE_REQUIRED, null, sessionUser.getLocale());
            errors.add(new ErrorMessage("maxGrade", message));
        } else if (exercise.getMaxGrade() < 1 || exercise.getMaxGrade() > 15) {
            String message = messageSource.getMessage(MessageKeys.EXERCISE_VALIDATION_MAX_GRADE_INVALID, null, sessionUser.getLocale());
            errors.add(new ErrorMessage("maxGrade", message));
        }
    }

    private Integer getTeacherId() {
        return Integer.valueOf(
                sessionUser.getParameters().get(SessionParameter.TEACHER_ID.getClaimName())
        );
    }

    private void validatePercentageGradeSum(Integer subjectClassId, Integer quarter, Integer percentageGrade, Integer excludeExerciseId, List<ErrorMessage> errors) {
        if (quarter == null || percentageGrade == null || subjectClassId == null) {
            return;
        }

        Integer currentSum;
        if (excludeExerciseId != null) {
            currentSum = exerciseRepository.sumPercentageGradeBySubjectClassIdAndQuarterExcludingId(subjectClassId, quarter, excludeExerciseId);
        } else {
            currentSum = exerciseRepository.sumPercentageGradeBySubjectClassIdAndQuarter(subjectClassId, quarter);
        }

        int totalSum = currentSum + percentageGrade;
        if (totalSum > 100) {
            String message = messageSource.getMessage(
                    MessageKeys.EXERCISE_VALIDATION_PERCENTAGE_GRADE_SUM_EXCEEDED,
                    new Object[]{totalSum, currentSum},
                    sessionUser.getLocale()
            );
            errors.add(new ErrorMessage(FIELD_PERCENTAGE_GRADE, message));
        }
    }
}

