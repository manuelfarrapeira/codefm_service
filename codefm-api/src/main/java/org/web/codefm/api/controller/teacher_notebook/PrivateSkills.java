package org.web.codefm.api.controller.teacher_notebook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.web.codefm.api.TeacherNoteBookSkillsApi;
import org.web.codefm.api.mapper.SkillDTOMapper;
import org.web.codefm.api.mapper.SkillRequestMapper;
import org.web.codefm.api.utils.Locale;
import org.web.codefm.api.utils.Logged;
import org.web.codefm.domain.entity.teachernotebook.Skill;
import org.web.codefm.domain.usecase.teachernotebook.SkillUseCase;
import org.web.codefm.model.SkillDTO;
import org.web.codefm.model.SkillRequestDTO;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PrivateSkills implements TeacherNoteBookSkillsApi {

    private final SkillUseCase skillUseCase;
    private final SkillDTOMapper skillDTOMapper;
    private final SkillRequestMapper skillRequestMapper;

    @Logged
    @Override
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<SkillDTO>> skills() {
        return ResponseEntity.ok(this.skillDTOMapper.toDTOList(this.skillUseCase.getSkillsByTeacher()));
    }

    @Logged
    @Override
    @Locale(1)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<SkillDTO> createSkill(SkillRequestDTO skillRequestDTO, String acceptLanguage) {
        final Skill createdSkill = this.skillUseCase.createSkill(this.skillRequestMapper.toDomain(skillRequestDTO));
        return new ResponseEntity<>(this.skillDTOMapper.toDTO(createdSkill), HttpStatus.CREATED);
    }

    @Logged
    @Override
    @Locale(2)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<SkillDTO> updateSkill(Integer id, SkillRequestDTO skillRequestDTO, String acceptLanguage) {
        final Skill updatedSkill = this.skillUseCase.updateSkill(id, this.skillRequestMapper.toDomain(skillRequestDTO));
        return ResponseEntity.ok(this.skillDTOMapper.toDTO(updatedSkill));
    }

    @Logged
    @Override
    @Locale(1)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> deleteSkill(Integer id, String acceptLanguage) {
        this.skillUseCase.softDeleteSkill(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

