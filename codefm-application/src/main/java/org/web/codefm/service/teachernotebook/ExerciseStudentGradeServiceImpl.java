package org.web.codefm.service.teachernotebook;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.web.codefm.domain.entity.exception.ErrorMessage;
import org.web.codefm.domain.entity.teachernotebook.*;
import org.web.codefm.domain.exception.teachernotebook.ClassForbiddenException;
import org.web.codefm.domain.exception.teachernotebook.ClassNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.ExerciseStudentGradeNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.ExerciseStudentGradeValidationException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.repository.teachernotebook.*;
import org.web.codefm.domain.service.teachernotebook.ExerciseStudentGradeService;
import org.web.codefm.domain.session.SessionParameter;
import org.web.codefm.domain.session.SessionUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExerciseStudentGradeServiceImpl implements ExerciseStudentGradeService {

    private static final String FIELD_GRADE = "grade";
    private static final String FIELD_STUDENT_ID = "studentId";

    private final ExerciseStudentGradeRepository exerciseStudentGradeRepository;
    private final ExerciseRepository exerciseRepository;
    private final ClassRepository classRepository;
    private final StudentRepository studentRepository;
    private final StudentClassRepository studentClassRepository;
    private final SubjectClassRepository subjectClassRepository;
    private final MessageSource messageSource;
    private final SessionUser sessionUser;

    @Override
    public List<ExerciseStudentGrade> getGradesByClassId(Integer classId) {
        Integer teacherId = getTeacherId();
        Locale locale = sessionUser.getLocale();

        validateClassOwnership(classId, teacherId, locale);

        return exerciseStudentGradeRepository.findByClassId(classId);
    }

    @Override
    public List<ExerciseStudentGrade> getGradesByClassIdAndStudentId(Integer classId, Integer studentId) {
        Integer teacherId = getTeacherId();
        Locale locale = sessionUser.getLocale();

        validateClassOwnership(classId, teacherId, locale);
        validateStudentInClass(studentId, classId, teacherId, locale);

        return exerciseStudentGradeRepository.findByClassIdAndStudentId(classId, studentId);
    }

    @Override
    public ExerciseStudentGrade createGrade(Integer exerciseId, ExerciseStudentGrade grade) {
        Integer teacherId = getTeacherId();
        Locale locale = sessionUser.getLocale();
        List<ErrorMessage> errors = new ArrayList<>();

        Exercise exercise = exerciseRepository.findByIdAndTeacherId(exerciseId, teacherId)
                .orElseThrow(() -> new ExerciseStudentGradeNotFoundException(
                        messageSource.getMessage(MessageKeys.EXERCISE_STUDENT_GRADE_EXERCISE_NOT_FOUND, null, locale)
                ));

        validateStudentId(grade.getStudentId(), teacherId, errors, locale);
        validateGrade(grade, exercise, errors, locale);

        if (!errors.isEmpty()) {
            throw new ExerciseStudentGradeValidationException(errors);
        }

        validateStudentInExerciseClass(grade.getStudentId(), exercise, teacherId, errors, locale);
        validateNoDuplicate(grade.getStudentId(), exerciseId, teacherId, errors, locale);

        if (!errors.isEmpty()) {
            throw new ExerciseStudentGradeValidationException(errors);
        }

        ExerciseStudentGrade gradeToSave = ExerciseStudentGrade.builder()
                .studentId(grade.getStudentId())
                .exerciseId(exerciseId)
                .grade(grade.getGrade())
                .description(grade.getDescription())
                .build();

        return exerciseStudentGradeRepository.save(gradeToSave);
    }

    @Override
    public ExerciseStudentGrade updateGrade(Integer id, ExerciseStudentGrade grade) {
        Integer teacherId = getTeacherId();
        Locale locale = sessionUser.getLocale();
        List<ErrorMessage> errors = new ArrayList<>();

        ExerciseStudentGrade existingGrade = exerciseStudentGradeRepository.findByIdAndTeacherId(id, teacherId)
                .orElseThrow(() -> new ExerciseStudentGradeNotFoundException(
                        messageSource.getMessage(MessageKeys.EXERCISE_STUDENT_GRADE_NOT_FOUND, null, locale)
                ));

        Exercise exercise = exerciseRepository.findByIdAndTeacherId(existingGrade.getExerciseId(), teacherId)
                .orElseThrow(() -> new ExerciseStudentGradeNotFoundException(
                        messageSource.getMessage(MessageKeys.EXERCISE_STUDENT_GRADE_EXERCISE_NOT_FOUND, null, locale)
                ));

        validateGrade(grade, exercise, errors, locale);

        if (!errors.isEmpty()) {
            throw new ExerciseStudentGradeValidationException(errors);
        }

        existingGrade.setGrade(grade.getGrade());
        existingGrade.setDescription(grade.getDescription());

        return exerciseStudentGradeRepository.update(existingGrade);
    }

    @Override
    public void deleteGrade(Integer id) {
        Integer teacherId = getTeacherId();
        Locale locale = sessionUser.getLocale();

        exerciseStudentGradeRepository.findByIdAndTeacherId(id, teacherId)
                .orElseThrow(() -> new ExerciseStudentGradeNotFoundException(
                        messageSource.getMessage(MessageKeys.EXERCISE_STUDENT_GRADE_NOT_FOUND, null, locale)
                ));

        exerciseStudentGradeRepository.softDelete(id);
    }

    private void validateStudentId(Integer studentId, Integer teacherId, List<ErrorMessage> errors, Locale locale) {
        if (studentId == null) {
            String message = messageSource.getMessage(MessageKeys.EXERCISE_STUDENT_GRADE_STUDENT_REQUIRED, null, locale);
            errors.add(new ErrorMessage(FIELD_STUDENT_ID, message));
            return;
        }

        Optional<Student> student = studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId);
        if (student.isEmpty()) {
            String message = messageSource.getMessage(MessageKeys.EXERCISE_STUDENT_GRADE_STUDENT_NOT_FOUND, null, locale);
            errors.add(new ErrorMessage(FIELD_STUDENT_ID, message));
        }
    }

    private void validateGrade(ExerciseStudentGrade grade, Exercise exercise, List<ErrorMessage> errors, Locale locale) {
        if (grade.getGrade() == null) {
            String message = messageSource.getMessage(MessageKeys.EXERCISE_STUDENT_GRADE_REQUIRED, null, locale);
            errors.add(new ErrorMessage(FIELD_GRADE, message));
        } else if (grade.getGrade() > exercise.getMaxGrade()) {
            String message = messageSource.getMessage(
                    MessageKeys.EXERCISE_STUDENT_GRADE_EXCEEDS_MAX,
                    new Object[]{grade.getGrade(), exercise.getMaxGrade()},
                    locale
            );
            errors.add(new ErrorMessage(FIELD_GRADE, message));
        }
    }

    private void validateStudentInExerciseClass(Integer studentId, Exercise exercise, Integer teacherId, List<ErrorMessage> errors, Locale locale) {
        Optional<SubjectClass> subjectClass = subjectClassRepository.findById(exercise.getSubjectClassId());
        if (subjectClass.isEmpty()) {
            return;
        }

        Integer classId = subjectClass.get().getClassId();
        Optional<StudentClass> studentClass = studentClassRepository.findByClassIdAndStudentId(classId, studentId);

        if (studentClass.isEmpty() || studentClass.get().getDeletionDate() != null) {
            String studentName = getStudentFullName(studentId, teacherId);
            String message = messageSource.getMessage(
                    MessageKeys.EXERCISE_STUDENT_GRADE_STUDENT_NOT_IN_CLASS,
                    new Object[]{studentName},
                    locale
            );
            errors.add(new ErrorMessage(FIELD_STUDENT_ID, message));
        }
    }

    private void validateStudentInClass(Integer studentId, Integer classId, Integer teacherId, Locale locale) {
        Optional<StudentClass> studentClass = studentClassRepository.findByClassIdAndStudentId(classId, studentId);

        if (studentClass.isEmpty() || studentClass.get().getDeletionDate() != null) {
            String studentName = getStudentFullName(studentId, teacherId);
            String message = messageSource.getMessage(
                    MessageKeys.EXERCISE_STUDENT_GRADE_STUDENT_NOT_IN_CLASS,
                    new Object[]{studentName},
                    locale
            );
            List<ErrorMessage> errors = new ArrayList<>();
            errors.add(new ErrorMessage(FIELD_STUDENT_ID, message));
            throw new ExerciseStudentGradeValidationException(errors);
        }
    }

    private void validateNoDuplicate(Integer studentId, Integer exerciseId, Integer teacherId, List<ErrorMessage> errors, Locale locale) {
        if (exerciseStudentGradeRepository.existsByStudentIdAndExerciseIdAndDeletionDateIsNull(studentId, exerciseId)) {
            String studentName = getStudentFullName(studentId, teacherId);
            String message = messageSource.getMessage(
                    MessageKeys.EXERCISE_STUDENT_GRADE_ALREADY_EXISTS,
                    new Object[]{studentName},
                    locale
            );
            errors.add(new ErrorMessage(FIELD_STUDENT_ID, message));
        }
    }

    private void validateClassOwnership(Integer classId, Integer teacherId, Locale locale) {
        classRepository.findById(classId)
                .orElseThrow(() -> new ClassNotFoundException(
                        messageSource.getMessage(MessageKeys.CLASS_NOT_FOUND, null, locale)
                ));

        classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId)
                .orElseThrow(() -> new ClassForbiddenException(
                        messageSource.getMessage(MessageKeys.CLASS_FORBIDDEN, null, locale)
                ));
    }

    private String getStudentFullName(Integer studentId, Integer teacherId) {
        return studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId)
                .map(s -> s.getName() + " " + s.getSurnames())
                .orElse(String.valueOf(studentId));
    }

    private Integer getTeacherId() {
        return sessionUser.getParameter(SessionParameter.TEACHER_ID, Integer.class);
    }
}

