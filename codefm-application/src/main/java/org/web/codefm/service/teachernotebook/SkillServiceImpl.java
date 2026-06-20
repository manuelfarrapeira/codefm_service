package org.web.codefm.service.teachernotebook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web.codefm.domain.entity.exception.ErrorMessage;
import org.web.codefm.domain.entity.teachernotebook.Skill;
import org.web.codefm.domain.exception.teachernotebook.SkillForbiddenException;
import org.web.codefm.domain.exception.teachernotebook.SkillNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.SkillValidationException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.repository.teachernotebook.SkillRepository;
import org.web.codefm.domain.service.teachernotebook.SkillService;
import org.web.codefm.domain.session.SessionParameter;
import org.web.codefm.domain.session.SessionUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;
    private final MessageSource messageSource;
    private final SessionUser sessionUser;

    private static final int MIN_FIELD_LENGTH = 5;

    @Override
    public List<Skill> getSkillsByTeacher() {
        final Integer teacherId = this.sessionUser.getParameter(SessionParameter.TEACHER_ID);
        return this.skillRepository.findByTeacherId(teacherId);
    }

    @Override
    public Skill createSkill(Skill skill) {
        final List<ErrorMessage> errors = new ArrayList<>();
        final Locale locale = this.sessionUser.getLocale();

        this.validateSkill(skill, errors, locale);

        if (!errors.isEmpty()) {
            throw new SkillValidationException(errors);
        }

        final Integer teacherId = this.sessionUser.getParameter(SessionParameter.TEACHER_ID);
        skill.setTeacherId(teacherId);

        return this.skillRepository.save(skill);
    }

    @Override
    @Transactional
    public Skill updateSkill(Integer skillId, Skill skill) {
        final Integer teacherId = this.sessionUser.getParameter(SessionParameter.TEACHER_ID);
        final Locale locale = this.sessionUser.getLocale();
        final List<ErrorMessage> errors = new ArrayList<>();

        this.validateSkill(skill, errors, locale);

        if (!errors.isEmpty()) {
            throw new SkillValidationException(errors);
        }

        final Skill existingSkill = this.validateSkillOwnership(skillId, teacherId, locale);

        existingSkill.setTitle(skill.getTitle());
        existingSkill.setDescription(skill.getDescription());

        return this.skillRepository.save(existingSkill);
    }

    @Override
    @Transactional
    public void softDeleteSkill(Integer skillId) {
        final Integer teacherId = this.sessionUser.getParameter(SessionParameter.TEACHER_ID);
        final Locale locale = this.sessionUser.getLocale();
        this.validateSkillOwnership(skillId, teacherId, locale);
        this.skillRepository.softDeleteSkill(skillId, teacherId);
    }

    private void validateSkill(Skill skill, List<ErrorMessage> errors, Locale locale) {
        if (skill.getTitle() == null || skill.getTitle().trim().isEmpty()) {
            final String message = this.messageSource.getMessage(MessageKeys.SKILL_VALIDATION_TITLE_REQUIRED, null, locale);
            errors.add(new ErrorMessage("title", message));
        } else if (skill.getTitle().trim().length() < MIN_FIELD_LENGTH) {
            final String message = this.messageSource.getMessage(MessageKeys.SKILL_VALIDATION_TITLE_MIN_LENGTH, null, locale);
            errors.add(new ErrorMessage("title", message));
        }

        if (skill.getDescription() == null || skill.getDescription().trim().isEmpty()) {
            final String message = this.messageSource.getMessage(MessageKeys.SKILL_VALIDATION_DESCRIPTION_REQUIRED, null, locale);
            errors.add(new ErrorMessage("description", message));
        } else if (skill.getDescription().trim().length() < MIN_FIELD_LENGTH) {
            final String message = this.messageSource.getMessage(MessageKeys.SKILL_VALIDATION_DESCRIPTION_MIN_LENGTH, null, locale);
            errors.add(new ErrorMessage("description", message));
        }
    }

    private Skill validateSkillOwnership(Integer skillId, Integer teacherId, Locale locale) {
        final Optional<Skill> skillOpt = this.skillRepository.findById(skillId);

        if (skillOpt.isEmpty()) {
            final String message = this.messageSource.getMessage(MessageKeys.SKILL_NOT_FOUND, null, locale);
            throw new SkillNotFoundException(message);
        }

        final Skill skill = skillOpt.get();

        if (!skill.getTeacherId().equals(teacherId)) {
            final String message = this.messageSource.getMessage(MessageKeys.SKILL_FORBIDDEN, null, locale);
            throw new SkillForbiddenException(message);
        }

        return skill;
    }
}

