package org.web.codefm.service.teachernotebook;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.web.codefm.domain.entity.exception.ErrorMessage;
import org.web.codefm.domain.entity.teachernotebook.*;
import org.web.codefm.domain.exception.teachernotebook.ClassRubricNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.ClassRubricValidationException;
import org.web.codefm.domain.exception.teachernotebook.StudentClassRubricCriteriaNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.StudentClassRubricCriteriaValidationException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.repository.teachernotebook.*;
import org.web.codefm.domain.service.teachernotebook.ClassRubricService;
import org.web.codefm.domain.session.SessionParameter;
import org.web.codefm.domain.session.SessionUser;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ClassRubricServiceImpl implements ClassRubricService {

    private final ClassRubricRepository classRubricRepository;
    private final StudentClassRubricCriteriaRepository studentClassRubricCriteriaRepository;
    private final ClassRepository classRepository;
    private final SkillRubricRepository skillRubricRepository;
    private final SkillRepository skillRepository;
    private final StudentRepository studentRepository;
    private final StudentClassRepository studentClassRepository;
    private final SkillRubricCriteriaRepository skillRubricCriteriaRepository;
    private final MessageSource messageSource;
    private final SessionUser sessionUser;

    @Override
    public List<ClassRubric> getRubricsByClassId(Integer classId) {
        final Integer teacherId = this.getTeacherId();
        final Locale locale = this.sessionUser.getLocale();
        this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId)
                .orElseThrow(() -> new ClassRubricNotFoundException(
                        this.messageSource.getMessage(MessageKeys.CLASS_RUBRIC_VALIDATION_CLASS_NOT_FOUND, null, locale)));
        return this.classRubricRepository.findByClassId(classId);
    }

    @Override
    public ClassRubric assignRubricToClass(Integer classId, Integer rubricId) {
        final Integer teacherId = this.getTeacherId();
        final Locale locale = this.sessionUser.getLocale();
        final List<ErrorMessage> errors = new ArrayList<>();

        this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId)
                .orElseGet(() -> {
                    errors.add(new ErrorMessage("classId",
                            this.messageSource.getMessage(MessageKeys.CLASS_RUBRIC_VALIDATION_CLASS_NOT_FOUND, null, locale)));
                    return null;
                });

        final Optional<SkillRubric> rubricOpt = this.skillRubricRepository.findById(rubricId);
        if (rubricOpt.isEmpty()) {
            errors.add(new ErrorMessage("rubricId",
                    this.messageSource.getMessage(MessageKeys.CLASS_RUBRIC_VALIDATION_RUBRIC_NOT_FOUND, null, locale)));
        } else {
            this.skillRepository.findByIdAndTeacherId(rubricOpt.get().getSkillId(), teacherId)
                    .orElseGet(() -> {
                        errors.add(new ErrorMessage("rubricId",
                                this.messageSource.getMessage(MessageKeys.CLASS_RUBRIC_VALIDATION_RUBRIC_NOT_FOUND, null, locale)));
                        return null;
                    });
        }

        if (errors.isEmpty() && this.classRubricRepository
                .existsByClassIdAndRubricIdAndDeletionDateIsNull(classId, rubricId)) {
            errors.add(new ErrorMessage("rubricId",
                    this.messageSource.getMessage(MessageKeys.CLASS_RUBRIC_ALREADY_EXISTS, null, locale)));
        }

        if (!errors.isEmpty()) {
            throw new ClassRubricValidationException(errors);
        }

        final ClassRubric classRubric = ClassRubric.builder()
                .classId(classId)
                .rubricId(rubricId)
                .build();
        return this.classRubricRepository.save(classRubric);
    }

    @Override
    public void removeRubricFromClass(Integer classRubricId) {
        final Integer teacherId = this.getTeacherId();
        final Locale locale = this.sessionUser.getLocale();
        this.classRubricRepository.findByIdAndTeacherId(classRubricId, teacherId)
                .orElseThrow(() -> new ClassRubricNotFoundException(
                        this.messageSource.getMessage(MessageKeys.CLASS_RUBRIC_NOT_FOUND, null, locale)));
        this.classRubricRepository.softDeleteById(classRubricId);
    }

    @Override
    public List<StudentCriteriaGroup> getAllStudentCriteriaByClassId(Integer classId) {
        final Integer teacherId = this.getTeacherId();
        final Locale locale = this.sessionUser.getLocale();
        this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId)
                .orElseThrow(() -> new ClassRubricNotFoundException(
                        this.messageSource.getMessage(MessageKeys.CLASS_RUBRIC_VALIDATION_CLASS_NOT_FOUND, null, locale)));
        final List<StudentClassRubricCriteria> flatList = this.studentClassRubricCriteriaRepository.findByClassId(classId);
        return this.groupByStudent(flatList);
    }

    private List<StudentCriteriaGroup> groupByStudent(List<StudentClassRubricCriteria> flatList) {
        final Map<Integer, StudentCriteriaGroup> grouped = new LinkedHashMap<>();
        for (final StudentClassRubricCriteria item : flatList) {
            grouped.computeIfAbsent(item.getStudentId(), k -> StudentCriteriaGroup.builder()
                    .studentId(item.getStudentId())
                    .studentName(item.getStudentName())
                    .studentSurnames(item.getStudentSurnames())
                    .rubricCriteria(new ArrayList<>())
                    .build());
            grouped.get(item.getStudentId()).getRubricCriteria().add(
                    RubricCriterionAssignment.builder()
                            .id(item.getId())
                            .classRubricId(item.getClassRubricId())
                            .rubricId(item.getRubricId())
                            .rubricTitle(item.getRubricTitle())
                            .criterionId(item.getCriterionId())
                            .criterionDescription(item.getCriterionDescription())
                            .gradeStart(item.getGradeStart())
                            .gradeEnd(item.getGradeEnd())
                            .build());
        }
        return new ArrayList<>(grouped.values());
    }

    @Override
    public List<StudentCriteriaGroup> getStudentCriteriaByClassAndStudent(Integer classId, Integer studentId) {
        final Integer teacherId = this.getTeacherId();
        final Locale locale = this.sessionUser.getLocale();
        this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId)
                .orElseThrow(() -> new ClassRubricNotFoundException(
                        this.messageSource.getMessage(MessageKeys.CLASS_RUBRIC_VALIDATION_CLASS_NOT_FOUND, null, locale)));
        this.studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId)
                .orElseThrow(() -> new StudentClassRubricCriteriaNotFoundException(
                        this.messageSource.getMessage(MessageKeys.STUDENT_CLASS_RUBRIC_CRITERIA_VALIDATION_STUDENT_NOT_FOUND, null, locale)));
        final List<StudentClassRubricCriteria> flatList = this.studentClassRubricCriteriaRepository.findByClassIdAndStudentId(classId, studentId);
        return this.groupByStudent(flatList);
    }

    @Override
    public StudentClassRubricCriteria assignCriterionToStudent(Integer classRubricId, Integer studentId, Integer criterionId) {
        final Integer teacherId = this.getTeacherId();
        final Locale locale = this.sessionUser.getLocale();

        final ClassRubric classRubric = this.classRubricRepository.findByIdAndTeacherId(classRubricId, teacherId)
                .orElseThrow(() -> new ClassRubricNotFoundException(
                        this.messageSource.getMessage(MessageKeys.CLASS_RUBRIC_NOT_FOUND, null, locale)));

        final List<ErrorMessage> errors = new ArrayList<>();

        this.studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId)
                .orElseGet(() -> {
                    errors.add(new ErrorMessage("studentId",
                            this.messageSource.getMessage(MessageKeys.STUDENT_CLASS_RUBRIC_CRITERIA_VALIDATION_STUDENT_NOT_FOUND, null, locale)));
                    return null;
                });

        if (errors.isEmpty()) {
            this.studentClassRepository.findByClassIdAndStudentId(classRubric.getClassId(), studentId)
                    .filter(sc -> sc.getDeletionDate() == null)
                    .orElseGet(() -> {
                        errors.add(new ErrorMessage("studentId",
                                this.messageSource.getMessage(MessageKeys.STUDENT_CLASS_RUBRIC_CRITERIA_VALIDATION_STUDENT_NOT_IN_CLASS, null, locale)));
                        return null;
                    });
        }

        final Optional<SkillRubricCriteria> criterionOpt = this.skillRubricCriteriaRepository.findActiveById(criterionId);
        if (criterionOpt.isEmpty()) {
            errors.add(new ErrorMessage("criterionId",
                    this.messageSource.getMessage(MessageKeys.STUDENT_CLASS_RUBRIC_CRITERIA_VALIDATION_CRITERION_NOT_FOUND, null, locale)));
        } else if (!criterionOpt.get().getRubricId().equals(classRubric.getRubricId())) {
            errors.add(new ErrorMessage("criterionId",
                    this.messageSource.getMessage(MessageKeys.STUDENT_CLASS_RUBRIC_CRITERIA_VALIDATION_CRITERION_NOT_IN_RUBRIC, null, locale)));
        }

        if (errors.isEmpty() && this.studentClassRubricCriteriaRepository
                .existsByClassRubricIdAndStudentIdAndDeletionDateIsNull(classRubricId, studentId)) {
            errors.add(new ErrorMessage("studentId",
                    this.messageSource.getMessage(MessageKeys.STUDENT_CLASS_RUBRIC_CRITERIA_ALREADY_EXISTS, null, locale)));
        }

        if (!errors.isEmpty()) {
            throw new StudentClassRubricCriteriaValidationException(errors);
        }

        final StudentClassRubricCriteria toSave = StudentClassRubricCriteria.builder()
                .classRubricId(classRubricId)
                .studentId(studentId)
                .criterionId(criterionId)
                .build();
        return this.studentClassRubricCriteriaRepository.save(toSave);
    }

    @Override
    public StudentClassRubricCriteria updateStudentCriterion(Integer id, Integer criterionId) {
        final Integer teacherId = this.getTeacherId();
        final Locale locale = this.sessionUser.getLocale();

        final StudentClassRubricCriteria existing = this.studentClassRubricCriteriaRepository
                .findByIdAndTeacherId(id, teacherId)
                .orElseThrow(() -> new StudentClassRubricCriteriaNotFoundException(
                        this.messageSource.getMessage(MessageKeys.STUDENT_CLASS_RUBRIC_CRITERIA_NOT_FOUND, null, locale)));

        final ClassRubric classRubric = this.classRubricRepository
                .findByIdAndTeacherId(existing.getClassRubricId(), teacherId)
                .orElseThrow(() -> new ClassRubricNotFoundException(
                        this.messageSource.getMessage(MessageKeys.CLASS_RUBRIC_NOT_FOUND, null, locale)));

        final List<ErrorMessage> errors = new ArrayList<>();
        final Optional<SkillRubricCriteria> criterionOpt = this.skillRubricCriteriaRepository.findActiveById(criterionId);
        if (criterionOpt.isEmpty()) {
            errors.add(new ErrorMessage("criterionId",
                    this.messageSource.getMessage(MessageKeys.STUDENT_CLASS_RUBRIC_CRITERIA_VALIDATION_CRITERION_NOT_FOUND, null, locale)));
        } else if (!criterionOpt.get().getRubricId().equals(classRubric.getRubricId())) {
            errors.add(new ErrorMessage("criterionId",
                    this.messageSource.getMessage(MessageKeys.STUDENT_CLASS_RUBRIC_CRITERIA_VALIDATION_CRITERION_NOT_IN_RUBRIC, null, locale)));
        }

        if (!errors.isEmpty()) {
            throw new StudentClassRubricCriteriaValidationException(errors);
        }

        existing.setCriterionId(criterionId);
        return this.studentClassRubricCriteriaRepository.save(existing);
    }

    @Override
    public void removeStudentCriterion(Integer id) {
        final Integer teacherId = this.getTeacherId();
        final Locale locale = this.sessionUser.getLocale();
        this.studentClassRubricCriteriaRepository.findByIdAndTeacherId(id, teacherId)
                .orElseThrow(() -> new StudentClassRubricCriteriaNotFoundException(
                        this.messageSource.getMessage(MessageKeys.STUDENT_CLASS_RUBRIC_CRITERIA_NOT_FOUND, null, locale)));
        this.studentClassRubricCriteriaRepository.softDeleteById(id);
    }

    private Integer getTeacherId() {
        return this.sessionUser.getParameter(SessionParameter.TEACHER_ID);
    }
}

