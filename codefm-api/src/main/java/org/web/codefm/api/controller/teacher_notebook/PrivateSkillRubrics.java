package org.web.codefm.api.controller.teacher_notebook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.web.codefm.api.TeacherNoteBookSkillRubricsApi;
import org.web.codefm.api.mapper.SkillRubricCriteriaDTOMapper;
import org.web.codefm.api.mapper.SkillRubricDTOMapper;
import org.web.codefm.api.mapper.SkillRubricRequestMapper;
import org.web.codefm.api.utils.Locale;
import org.web.codefm.api.utils.Logged;
import org.web.codefm.domain.entity.teachernotebook.SkillRubric;
import org.web.codefm.domain.entity.teachernotebook.SkillRubricCriteria;
import org.web.codefm.domain.usecase.teachernotebook.SkillRubricUseCase;
import org.web.codefm.model.SkillRubricCriteriaDTO;
import org.web.codefm.model.SkillRubricCriteriaRequestDTO;
import org.web.codefm.model.SkillRubricDTO;
import org.web.codefm.model.SkillRubricRequestDTO;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PrivateSkillRubrics implements TeacherNoteBookSkillRubricsApi {

    private final SkillRubricUseCase skillRubricUseCase;
    private final SkillRubricDTOMapper skillRubricDTOMapper;
    private final SkillRubricCriteriaDTOMapper skillRubricCriteriaDTOMapper;
    private final SkillRubricRequestMapper skillRubricRequestMapper;

    @Logged
    @Override
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<SkillRubricDTO>> getRubricsBySkill(Integer skillId) {
        final List<SkillRubric> rubrics = this.skillRubricUseCase.getRubricsBySkillId(skillId);
        return ResponseEntity.ok(this.skillRubricDTOMapper.toDTOList(rubrics));
    }

    @Logged
    @Override
    @Locale(2)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<SkillRubricDTO> createRubric(Integer skillId, SkillRubricRequestDTO skillRubricRequestDTO, String acceptLanguage) {
        final SkillRubric rubric = this.skillRubricRequestMapper.toDomain(skillRubricRequestDTO);
        final SkillRubric createdRubric = this.skillRubricUseCase.createRubric(skillId, rubric);
        return new ResponseEntity<>(this.skillRubricDTOMapper.toDTO(createdRubric), HttpStatus.CREATED);
    }

    @Logged
    @Override
    @Locale(2)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<SkillRubricDTO> updateRubric(Integer rubricId, SkillRubricRequestDTO skillRubricRequestDTO, String acceptLanguage) {
        final SkillRubric rubric = this.skillRubricRequestMapper.toDomain(skillRubricRequestDTO);
        final SkillRubric updatedRubric = this.skillRubricUseCase.updateRubric(rubricId, rubric);
        return ResponseEntity.ok(this.skillRubricDTOMapper.toDTO(updatedRubric));
    }

    @Logged
    @Override
    @Locale(1)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> deleteRubric(Integer rubricId, String acceptLanguage) {
        this.skillRubricUseCase.deleteRubric(rubricId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Logged
    @Override
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<SkillRubricCriteriaDTO>> getCriteriaByRubric(Integer rubricId) {
        final List<SkillRubricCriteria> criteria = this.skillRubricUseCase.getCriteriaByRubricId(rubricId);
        return ResponseEntity.ok(this.skillRubricCriteriaDTOMapper.toDTOList(criteria));
    }

    @Logged
    @Override
    @Locale(2)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<SkillRubricCriteriaDTO> createCriterion(Integer rubricId, SkillRubricCriteriaRequestDTO dto, String acceptLanguage) {
        final SkillRubricCriteria criterion = this.skillRubricRequestMapper.criteriaToDomain(dto);
        final SkillRubricCriteria created = this.skillRubricUseCase.createCriterion(rubricId, criterion);
        return new ResponseEntity<>(this.skillRubricCriteriaDTOMapper.toDTO(created), HttpStatus.CREATED);
    }

    @Logged
    @Override
    @Locale(3)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<SkillRubricCriteriaDTO> updateCriterion(Integer rubricId, Integer criterionId, SkillRubricCriteriaRequestDTO dto, String acceptLanguage) {
        final SkillRubricCriteria criterion = this.skillRubricRequestMapper.criteriaToDomain(dto);
        final SkillRubricCriteria updated = this.skillRubricUseCase.updateCriterion(rubricId, criterionId, criterion);
        return ResponseEntity.ok(this.skillRubricCriteriaDTOMapper.toDTO(updated));
    }

    @Logged
    @Override
    @Locale(2)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> deleteCriterion(Integer rubricId, Integer criterionId, String acceptLanguage) {
        this.skillRubricUseCase.deleteCriterion(rubricId, criterionId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

