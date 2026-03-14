package org.web.codefm.infrastructure.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.Skill;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.SkillEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.SkillJPARepository;
import org.web.codefm.infrastructure.mapper.SkillMapper;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkillRepositoryImplTest {

    @Mock
    private SkillJPARepository skillJPARepository;

    @Mock
    private SkillMapper skillMapper;

    @InjectMocks
    private SkillRepositoryImpl skillRepository;

    @Test
    void findByTeacherId_shouldReturnSkillsWhenTeacherHasSkills() {
        final Integer teacherId = 1;
        final SkillEntity entity1 = new SkillEntity(1, "Skill One", "Critical thinking", teacherId, null);
        final SkillEntity entity2 = new SkillEntity(2, "Skill Two", "Problem solving", teacherId, null);
        final List<SkillEntity> entities = Arrays.asList(entity1, entity2);

        final Skill skill1 = Skill.builder().id(1).title("Skill One").description("Critical thinking").teacherId(teacherId).build();
        final Skill skill2 = Skill.builder().id(2).title("Skill Two").description("Problem solving").teacherId(teacherId).build();
        final List<Skill> expectedSkills = Arrays.asList(skill1, skill2);

        when(skillJPARepository.findByTeacherId(teacherId)).thenReturn(entities);
        when(skillMapper.toModelList(entities)).thenReturn(expectedSkills);

        final List<Skill> result = skillRepository.findByTeacherId(teacherId);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(skillJPARepository, times(1)).findByTeacherId(teacherId);
        verify(skillMapper, times(1)).toModelList(entities);
    }

    @Test
    void findByTeacherId_shouldReturnEmptyListWhenTeacherHasNoSkills() {
        final Integer teacherId = 1;
        when(skillJPARepository.findByTeacherId(teacherId)).thenReturn(Collections.emptyList());
        when(skillMapper.toModelList(Collections.emptyList())).thenReturn(Collections.emptyList());

        final List<Skill> result = skillRepository.findByTeacherId(teacherId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void save_shouldMapToEntityAndSaveAndMapBackToModel() {
        final Skill skillToSave = Skill.builder().title("Teamwork").description("Teamwork skill").teacherId(1).build();
        final SkillEntity skillEntity = new SkillEntity();
        final SkillEntity savedEntity = new SkillEntity(1, "Teamwork", "Teamwork skill", 1, null);
        final Skill savedSkill = Skill.builder().id(1).title("Teamwork").description("Teamwork skill").teacherId(1).build();

        when(skillMapper.toEntity(skillToSave)).thenReturn(skillEntity);
        when(skillJPARepository.save(skillEntity)).thenReturn(savedEntity);
        when(skillMapper.toModel(savedEntity)).thenReturn(savedSkill);

        final Skill result = skillRepository.save(skillToSave);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Teamwork", result.getDescription());
    }

    @Test
    void findById_shouldReturnSkillWhenFoundAndNotDeleted() {
        final Integer skillId = 1;
        final SkillEntity skillEntity = new SkillEntity(skillId, "Creativity", "Creative thinking", 1, null);
        final Skill expectedSkill = Skill.builder().id(skillId).title("Creativity").description("Creative thinking").teacherId(1).build();

        when(skillJPARepository.findByIdAndDeletionDateIsNull(skillId)).thenReturn(Optional.of(skillEntity));
        when(skillMapper.toModel(skillEntity)).thenReturn(expectedSkill);

        final Optional<Skill> result = skillRepository.findById(skillId);

        assertTrue(result.isPresent());
        assertEquals(expectedSkill, result.get());
    }

    @Test
    void findById_shouldReturnEmptyWhenNotFound() {
        final Integer skillId = 1;
        when(skillJPARepository.findByIdAndDeletionDateIsNull(skillId)).thenReturn(Optional.empty());

        final Optional<Skill> result = skillRepository.findById(skillId);

        assertFalse(result.isPresent());
        verify(skillMapper, never()).toModel(any(SkillEntity.class));
    }

    @Test
    void findByIdAndTeacherId_shouldReturnSkillWhenFoundAndOwnedByTeacher() {
        final Integer skillId = 1;
        final Integer teacherId = 101;
        final SkillEntity skillEntity = new SkillEntity(skillId, "Leadership", "Lead teams", teacherId, null);
        final Skill expectedSkill = Skill.builder().id(skillId).title("Leadership").description("Lead teams").teacherId(teacherId).build();

        when(skillJPARepository.findByIdAndTeacherIdAndDeletionDateIsNull(skillId, teacherId)).thenReturn(Optional.of(skillEntity));
        when(skillMapper.toModel(skillEntity)).thenReturn(expectedSkill);

        final Optional<Skill> result = skillRepository.findByIdAndTeacherId(skillId, teacherId);

        assertTrue(result.isPresent());
        assertEquals(expectedSkill, result.get());
    }

    @Test
    void findByIdAndTeacherId_shouldReturnEmptyWhenNotFoundOrNotOwnedByTeacher() {
        final Integer skillId = 1;
        final Integer teacherId = 101;
        when(skillJPARepository.findByIdAndTeacherIdAndDeletionDateIsNull(skillId, teacherId)).thenReturn(Optional.empty());

        final Optional<Skill> result = skillRepository.findByIdAndTeacherId(skillId, teacherId);

        assertFalse(result.isPresent());
        verify(skillMapper, never()).toModel(any(SkillEntity.class));
    }

    @Test
    void softDeleteSkill_shouldSetDeletionDateAndReturnUpdatedSkill() {
        final Integer skillId = 1;
        final Integer teacherId = 101;
        final SkillEntity skillEntity = new SkillEntity(skillId, "Adaptability", "Adapt to change", teacherId, null);
        final Skill updatedSkill = Skill.builder().id(skillId).title("Adaptability").description("Adapt to change").teacherId(teacherId).deletionDate(LocalDate.now()).build();

        when(skillJPARepository.findByIdAndTeacherIdAndDeletionDateIsNull(skillId, teacherId)).thenReturn(Optional.of(skillEntity));
        when(skillJPARepository.save(any(SkillEntity.class))).thenReturn(skillEntity);
        when(skillMapper.toModel(any(SkillEntity.class))).thenReturn(updatedSkill);

        final Skill result = skillRepository.softDeleteSkill(skillId, teacherId);

        assertNotNull(result);
        assertNotNull(result.getDeletionDate());
        verify(skillJPARepository, times(1)).save(skillEntity);
    }

    @Test
    void softDeleteSkill_shouldThrowExceptionWhenSkillNotFoundOrNotOwned() {
        final Integer skillId = 1;
        final Integer teacherId = 101;
        when(skillJPARepository.findByIdAndTeacherIdAndDeletionDateIsNull(skillId, teacherId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> skillRepository.softDeleteSkill(skillId, teacherId));
        verify(skillJPARepository, never()).save(any(SkillEntity.class));
    }
}

