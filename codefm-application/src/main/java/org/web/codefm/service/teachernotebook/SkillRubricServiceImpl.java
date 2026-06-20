package org.web.codefm.service.teachernotebook;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.web.codefm.domain.entity.exception.ErrorMessage;
import org.web.codefm.domain.entity.teachernotebook.Skill;
import org.web.codefm.domain.entity.teachernotebook.SkillRubric;
import org.web.codefm.domain.entity.teachernotebook.SkillRubricCriteria;
import org.web.codefm.domain.exception.teachernotebook.SkillForbiddenException;
import org.web.codefm.domain.exception.teachernotebook.SkillNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.SkillRubricNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.SkillRubricValidationException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.repository.teachernotebook.SkillRepository;
import org.web.codefm.domain.repository.teachernotebook.SkillRubricCriteriaRepository;
import org.web.codefm.domain.repository.teachernotebook.SkillRubricRepository;
import org.web.codefm.domain.service.teachernotebook.SkillRubricService;
import org.web.codefm.domain.session.SessionParameter;
import org.web.codefm.domain.session.SessionUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SkillRubricServiceImpl implements SkillRubricService {

    private final SkillRubricRepository skillRubricRepository;
    private final SkillRubricCriteriaRepository skillRubricCriteriaRepository;
    private final SkillRepository skillRepository;
    private final MessageSource messageSource;
    private final SessionUser sessionUser;

    private static final int GRADE_MIN = 0;
    private static final int GRADE_MAX = 10;

    @Override
    public List<SkillRubric> getRubricsBySkillId(Integer skillId) {
        final Integer teacherId = this.getTeacherId();
        final Locale locale = this.sessionUser.getLocale();
        this.validateSkillOwnership(skillId, teacherId, locale);

        final List<SkillRubric> rubrics = this.skillRubricRepository.findBySkillId(skillId);
        for (final SkillRubric rubric : rubrics) {
            rubric.setCriteria(this.skillRubricCriteriaRepository.findActiveByRubricId(rubric.getId()));
        }
        return rubrics;
    }

    @Override
    public SkillRubric createRubric(Integer skillId, SkillRubric rubric) {
        final Integer teacherId = this.getTeacherId();
        final Locale locale = this.sessionUser.getLocale();
        this.validateSkillOwnership(skillId, teacherId, locale);

        final List<ErrorMessage> errors = new ArrayList<>();
        this.validateTitle(rubric, errors, locale);
        if (!errors.isEmpty()) {
            throw new SkillRubricValidationException(errors);
        }

        rubric.setSkillId(skillId);
        return this.skillRubricRepository.save(rubric);
    }

    @Override
    public SkillRubric updateRubric(Integer rubricId, SkillRubric rubric) {
        final Integer teacherId = this.getTeacherId();
        final Locale locale = this.sessionUser.getLocale();

        final SkillRubric existingRubric = this.findRubricOrThrow(rubricId, locale);
        this.validateSkillOwnership(existingRubric.getSkillId(), teacherId, locale);

        final List<ErrorMessage> errors = new ArrayList<>();
        this.validateTitle(rubric, errors, locale);
        if (!errors.isEmpty()) {
            throw new SkillRubricValidationException(errors);
        }

        existingRubric.setTitle(rubric.getTitle());
        return this.skillRubricRepository.save(existingRubric);
    }

    @Override
    public void deleteRubric(Integer rubricId) {
        final Integer teacherId = this.getTeacherId();
        final Locale locale = this.sessionUser.getLocale();

        final SkillRubric existingRubric = this.findRubricOrThrow(rubricId, locale);
        this.validateSkillOwnership(existingRubric.getSkillId(), teacherId, locale);

        this.skillRubricRepository.softDeleteById(rubricId);
    }

    @Override
    public List<SkillRubricCriteria> getCriteriaByRubricId(Integer rubricId) {
        final Integer teacherId = this.getTeacherId();
        final Locale locale = this.sessionUser.getLocale();

        final SkillRubric rubric = this.findRubricOrThrow(rubricId, locale);
        this.validateSkillOwnership(rubric.getSkillId(), teacherId, locale);

        return this.skillRubricCriteriaRepository.findActiveByRubricId(rubricId);
    }

    @Override
    public SkillRubricCriteria createCriterion(Integer rubricId, SkillRubricCriteria criterion) {
        final Integer teacherId = this.getTeacherId();
        final Locale locale = this.sessionUser.getLocale();

        final SkillRubric rubric = this.findRubricOrThrow(rubricId, locale);
        this.validateSkillOwnership(rubric.getSkillId(), teacherId, locale);

        final List<ErrorMessage> errors = new ArrayList<>();
        this.validateCriterion(criterion, errors, locale);
        if (!errors.isEmpty()) {
            throw new SkillRubricValidationException(errors);
        }

        final List<SkillRubricCriteria> existingCriteria = this.skillRubricCriteriaRepository.findActiveByRubricId(rubricId);
        this.validateNoOverlap(criterion, existingCriteria, null, errors, locale);
        if (!errors.isEmpty()) {
            throw new SkillRubricValidationException(errors);
        }

        criterion.setRubricId(rubricId);
        return this.skillRubricCriteriaRepository.save(criterion);
    }

    @Override
    public SkillRubricCriteria updateCriterion(Integer rubricId, Integer criterionId, SkillRubricCriteria criterion) {
        final Integer teacherId = this.getTeacherId();
        final Locale locale = this.sessionUser.getLocale();

        final SkillRubric rubric = this.findRubricOrThrow(rubricId, locale);
        this.validateSkillOwnership(rubric.getSkillId(), teacherId, locale);

        final SkillRubricCriteria existing = this.findCriterionOrThrow(criterionId, locale);
        this.validateCriterionBelongsToRubric(existing, rubricId, locale);

        final List<ErrorMessage> errors = new ArrayList<>();
        this.validateCriterion(criterion, errors, locale);
        if (!errors.isEmpty()) {
            throw new SkillRubricValidationException(errors);
        }

        final List<SkillRubricCriteria> existingCriteria = this.skillRubricCriteriaRepository.findActiveByRubricId(rubricId);
        this.validateNoOverlap(criterion, existingCriteria, criterionId, errors, locale);
        if (!errors.isEmpty()) {
            throw new SkillRubricValidationException(errors);
        }

        existing.setDescription(criterion.getDescription());
        existing.setQualification(criterion.getQualification());
        existing.setGradeStart(criterion.getGradeStart());
        existing.setGradeEnd(criterion.getGradeEnd());
        return this.skillRubricCriteriaRepository.save(existing);
    }

    @Override
    public void deleteCriterion(Integer rubricId, Integer criterionId) {
        final Integer teacherId = this.getTeacherId();
        final Locale locale = this.sessionUser.getLocale();

        final SkillRubric rubric = this.findRubricOrThrow(rubricId, locale);
        this.validateSkillOwnership(rubric.getSkillId(), teacherId, locale);

        final SkillRubricCriteria existing = this.findCriterionOrThrow(criterionId, locale);
        this.validateCriterionBelongsToRubric(existing, rubricId, locale);

        this.skillRubricCriteriaRepository.softDeleteById(criterionId);
    }

    private void validateTitle(SkillRubric rubric, List<ErrorMessage> errors, Locale locale) {
        if (rubric == null || rubric.getTitle() == null || rubric.getTitle().trim().isEmpty()) {
            errors.add(new ErrorMessage("title",
                    this.messageSource.getMessage(MessageKeys.SKILL_RUBRIC_VALIDATION_TITLE_REQUIRED, null, locale)));
        }
    }

    private void validateCriterion(SkillRubricCriteria criterion, List<ErrorMessage> errors, Locale locale) {
        if (criterion == null) {
            errors.add(new ErrorMessage("description",
                    this.messageSource.getMessage(MessageKeys.SKILL_RUBRIC_VALIDATION_CRITERIA_DESCRIPTION_REQUIRED, null, locale)));
            errors.add(new ErrorMessage("gradeStart",
                    this.messageSource.getMessage(MessageKeys.SKILL_RUBRIC_VALIDATION_CRITERIA_GRADE_START_REQUIRED, null, locale)));
            errors.add(new ErrorMessage("gradeEnd",
                    this.messageSource.getMessage(MessageKeys.SKILL_RUBRIC_VALIDATION_CRITERIA_GRADE_END_REQUIRED, null, locale)));
            return;
        }

        if (criterion.getDescription() == null || criterion.getDescription().trim().isEmpty()) {
            errors.add(new ErrorMessage("description",
                    this.messageSource.getMessage(MessageKeys.SKILL_RUBRIC_VALIDATION_CRITERIA_DESCRIPTION_REQUIRED, null, locale)));
        }

        if (criterion.getGradeStart() == null) {
            errors.add(new ErrorMessage("gradeStart",
                    this.messageSource.getMessage(MessageKeys.SKILL_RUBRIC_VALIDATION_CRITERIA_GRADE_START_REQUIRED, null, locale)));
        }

        if (criterion.getGradeEnd() == null) {
            errors.add(new ErrorMessage("gradeEnd",
                    this.messageSource.getMessage(MessageKeys.SKILL_RUBRIC_VALIDATION_CRITERIA_GRADE_END_REQUIRED, null, locale)));
        }

        if (criterion.getGradeStart() != null && criterion.getGradeEnd() != null
                && (criterion.getGradeStart() < GRADE_MIN || criterion.getGradeStart() > GRADE_MAX
                || criterion.getGradeEnd() < GRADE_MIN || criterion.getGradeEnd() > GRADE_MAX
                || criterion.getGradeStart() > criterion.getGradeEnd())) {
            errors.add(new ErrorMessage("gradeRange",
                    this.messageSource.getMessage(MessageKeys.SKILL_RUBRIC_VALIDATION_CRITERIA_GRADE_RANGE_INVALID, null, locale)));
        }
    }

    private void validateNoOverlap(SkillRubricCriteria criterion, List<SkillRubricCriteria> existing, Integer excludeId, List<ErrorMessage> errors, Locale locale) {
        for (final SkillRubricCriteria c : existing) {
            if (excludeId != null && excludeId.equals(c.getId())) {
                continue;
            }
            if (criterion.getGradeStart() < c.getGradeEnd() && criterion.getGradeEnd() > c.getGradeStart()) {
                errors.add(new ErrorMessage("gradeRange",
                        this.messageSource.getMessage(MessageKeys.SKILL_RUBRIC_VALIDATION_CRITERIA_OVERLAP, null, locale)));
                return;
            }
        }
    }

    private void validateSkillOwnership(Integer skillId, Integer teacherId, Locale locale) {
        final Optional<Skill> skillOpt = this.skillRepository.findById(skillId);

        if (skillOpt.isEmpty()) {
            throw new SkillNotFoundException(
                    this.messageSource.getMessage(MessageKeys.SKILL_NOT_FOUND, null, locale));
        }

        if (!skillOpt.get().getTeacherId().equals(teacherId)) {
            throw new SkillForbiddenException(
                    this.messageSource.getMessage(MessageKeys.SKILL_FORBIDDEN, null, locale));
        }
    }

    private SkillRubric findRubricOrThrow(Integer rubricId, Locale locale) {
        return this.skillRubricRepository.findById(rubricId)
                .orElseThrow(() -> new SkillRubricNotFoundException(
                        this.messageSource.getMessage(MessageKeys.SKILL_RUBRIC_NOT_FOUND, null, locale)));
    }

    private SkillRubricCriteria findCriterionOrThrow(Integer criterionId, Locale locale) {
        return this.skillRubricCriteriaRepository.findActiveById(criterionId)
                .orElseThrow(() -> new SkillRubricNotFoundException(
                        this.messageSource.getMessage(MessageKeys.SKILL_RUBRIC_CRITERIA_NOT_FOUND, null, locale)));
    }

    private void validateCriterionBelongsToRubric(SkillRubricCriteria criterion, Integer rubricId, Locale locale) {
        if (!criterion.getRubricId().equals(rubricId)) {
            throw new SkillRubricNotFoundException(
                    this.messageSource.getMessage(MessageKeys.SKILL_RUBRIC_CRITERIA_NOT_FOUND, null, locale));
        }
    }


    private Integer getTeacherId() {
        return this.sessionUser.getParameter(SessionParameter.TEACHER_ID);
    }
}

