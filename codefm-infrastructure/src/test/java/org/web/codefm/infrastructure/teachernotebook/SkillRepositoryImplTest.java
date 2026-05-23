package org.web.codefm.infrastructure.teachernotebook;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.Skill;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.SkillEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.SkillJPARepository;
import org.web.codefm.infrastructure.mapper.SkillMapper;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkillRepositoryImplTest {

    private SkillRepositoryImpl skillRepository;

    @Mock
    private SkillJPARepository skillJPARepository;

    @Mock
    private SkillMapper skillMapper;

    @BeforeEach
    void beforeEach() {
        this.skillRepository = new SkillRepositoryImpl(this.skillJPARepository, this.skillMapper);
    }

    @Nested
    class FindByTeacherId {

        @Test
        void when_teacher_has_skills_expect_skills_returned() {
            final Integer teacherId = 1;
            final SkillEntity entity1 = new SkillEntity(1, "Skill One", "Critical thinking", teacherId, null);
            final SkillEntity entity2 = new SkillEntity(2, "Skill Two", "Problem solving", teacherId, null);
            final List<SkillEntity> entities = List.of(entity1, entity2);
            final Skill skill1 = Skill.builder().id(1).title("Skill One").description("Critical thinking").teacherId(teacherId).build();
            final Skill skill2 = Skill.builder().id(2).title("Skill Two").description("Problem solving").teacherId(teacherId).build();
            final List<Skill> expectedSkills = List.of(skill1, skill2);

            when(SkillRepositoryImplTest.this.skillJPARepository.findByTeacherId(teacherId)).thenReturn(entities);
            when(SkillRepositoryImplTest.this.skillMapper.toModelList(entities)).thenReturn(expectedSkills);

            final List<Skill> result = SkillRepositoryImplTest.this.skillRepository.findByTeacherId(teacherId);

            assertThat(result).isNotNull().hasSize(2);
            verify(SkillRepositoryImplTest.this.skillJPARepository, times(1)).findByTeacherId(teacherId);
            verify(SkillRepositoryImplTest.this.skillMapper, times(1)).toModelList(entities);
        }

        @Test
        void when_teacher_has_no_skills_expect_empty_list_returned() {
            final Integer teacherId = 1;

            when(SkillRepositoryImplTest.this.skillJPARepository.findByTeacherId(teacherId)).thenReturn(Collections.emptyList());
            when(SkillRepositoryImplTest.this.skillMapper.toModelList(Collections.emptyList())).thenReturn(Collections.emptyList());

            final List<Skill> result = SkillRepositoryImplTest.this.skillRepository.findByTeacherId(teacherId);

            assertThat(result).isNotNull().isEmpty();
        }
    }

    @Nested
    class Save {

        @Test
        void when_valid_skill_expect_skill_saved() {
            final Skill skillToSave = Skill.builder().title("Teamwork").description("Teamwork skill").teacherId(1).build();
            final SkillEntity skillEntity = new SkillEntity();
            final SkillEntity savedEntity = new SkillEntity(1, "Teamwork", "Teamwork skill", 1, null);
            final Skill savedSkill = Skill.builder().id(1).title("Teamwork").description("Teamwork skill").teacherId(1).build();

            when(SkillRepositoryImplTest.this.skillMapper.toEntity(skillToSave)).thenReturn(skillEntity);
            when(SkillRepositoryImplTest.this.skillJPARepository.save(skillEntity)).thenReturn(savedEntity);
            when(SkillRepositoryImplTest.this.skillMapper.toModel(savedEntity)).thenReturn(savedSkill);

            final Skill result = SkillRepositoryImplTest.this.skillRepository.save(skillToSave);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            assertThat(result.getDescription()).isEqualTo("Teamwork skill");
        }
    }

    @Nested
    class FindById {

        @Test
        void when_skill_is_found_and_not_deleted_expect_skill_returned() {
            final Integer skillId = 1;
            final SkillEntity skillEntity = new SkillEntity(skillId, "Creativity", "Creative thinking", 1, null);
            final Skill expectedSkill = Skill.builder().id(skillId).title("Creativity").description("Creative thinking").teacherId(1).build();

            when(SkillRepositoryImplTest.this.skillJPARepository.findByIdAndDeletionDateIsNull(skillId)).thenReturn(Optional.of(skillEntity));
            when(SkillRepositoryImplTest.this.skillMapper.toModel(skillEntity)).thenReturn(expectedSkill);

            final Optional<Skill> result = SkillRepositoryImplTest.this.skillRepository.findById(skillId);

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(expectedSkill);
        }

        @Test
        void when_skill_is_not_found_expect_empty_optional_returned() {
            final Integer skillId = 1;

            when(SkillRepositoryImplTest.this.skillJPARepository.findByIdAndDeletionDateIsNull(skillId)).thenReturn(Optional.empty());

            final Optional<Skill> result = SkillRepositoryImplTest.this.skillRepository.findById(skillId);

            assertThat(result).isNotPresent();
            verify(SkillRepositoryImplTest.this.skillMapper, never()).toModel(any(SkillEntity.class));
        }
    }

    @Nested
    class FindByIdAndTeacherId {

        @Test
        void when_skill_is_found_and_owned_by_teacher_expect_skill_returned() {
            final Integer skillId = 1;
            final Integer teacherId = 101;
            final SkillEntity skillEntity = new SkillEntity(skillId, "Leadership", "Lead teams", teacherId, null);
            final Skill expectedSkill = Skill.builder().id(skillId).title("Leadership").description("Lead teams").teacherId(teacherId).build();

            when(SkillRepositoryImplTest.this.skillJPARepository.findByIdAndTeacherIdAndDeletionDateIsNull(skillId, teacherId)).thenReturn(Optional.of(skillEntity));
            when(SkillRepositoryImplTest.this.skillMapper.toModel(skillEntity)).thenReturn(expectedSkill);

            final Optional<Skill> result = SkillRepositoryImplTest.this.skillRepository.findByIdAndTeacherId(skillId, teacherId);

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(expectedSkill);
        }

        @Test
        void when_skill_is_not_found_or_not_owned_expect_empty_optional_returned() {
            final Integer skillId = 1;
            final Integer teacherId = 101;

            when(SkillRepositoryImplTest.this.skillJPARepository.findByIdAndTeacherIdAndDeletionDateIsNull(skillId, teacherId)).thenReturn(Optional.empty());

            final Optional<Skill> result = SkillRepositoryImplTest.this.skillRepository.findByIdAndTeacherId(skillId, teacherId);

            assertThat(result).isNotPresent();
            verify(SkillRepositoryImplTest.this.skillMapper, never()).toModel(any(SkillEntity.class));
        }
    }

    @Nested
    class SoftDeleteSkill {

        @Test
        void when_skill_is_found_expect_deletion_date_set() {
            final Integer skillId = 1;
            final Integer teacherId = 101;
            final SkillEntity skillEntity = new SkillEntity(skillId, "Adaptability", "Adapt to change", teacherId, null);
            final Skill updatedSkill = Skill.builder().id(skillId).title("Adaptability").description("Adapt to change")
                    .teacherId(teacherId).deletionDate(LocalDate.now()).build();

            when(SkillRepositoryImplTest.this.skillJPARepository.findByIdAndTeacherIdAndDeletionDateIsNull(skillId, teacherId)).thenReturn(Optional.of(skillEntity));
            when(SkillRepositoryImplTest.this.skillJPARepository.save(any(SkillEntity.class))).thenReturn(skillEntity);
            when(SkillRepositoryImplTest.this.skillMapper.toModel(any(SkillEntity.class))).thenReturn(updatedSkill);

            final Skill result = SkillRepositoryImplTest.this.skillRepository.softDeleteSkill(skillId, teacherId);

            assertThat(result).isNotNull();
            assertThat(result.getDeletionDate()).isNotNull();
            verify(SkillRepositoryImplTest.this.skillJPARepository, times(1)).save(skillEntity);
        }

        @Test
        void when_skill_is_not_found_expect_exception_thrown() {
            final Integer skillId = 1;
            final Integer teacherId = 101;
            final ThrowingCallable callable = () -> SkillRepositoryImplTest.this.skillRepository.softDeleteSkill(skillId, teacherId);

            when(SkillRepositoryImplTest.this.skillJPARepository.findByIdAndTeacherIdAndDeletionDateIsNull(skillId, teacherId)).thenReturn(Optional.empty());

            assertThatThrownBy(callable).isInstanceOf(IllegalArgumentException.class);
            verify(SkillRepositoryImplTest.this.skillJPARepository, never()).save(any(SkillEntity.class));
        }
    }
}

