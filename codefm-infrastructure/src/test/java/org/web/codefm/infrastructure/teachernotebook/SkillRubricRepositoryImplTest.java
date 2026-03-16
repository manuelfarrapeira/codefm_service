package org.web.codefm.infrastructure.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.SkillRubric;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.SkillRubricEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.SkillRubricJPARepository;
import org.web.codefm.infrastructure.mapper.SkillRubricMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SkillRubricRepositoryImplTest {

    @Mock
    private SkillRubricJPARepository skillRubricJPARepository;
    @Mock
    private SkillRubricMapper skillRubricMapper;

    @InjectMocks
    private SkillRubricRepositoryImpl skillRubricRepository;

    @Test
    void findBySkillId_shouldReturnRubricsWhenExist() {
        final Integer skillId = 1;
        final SkillRubricEntity entity = new SkillRubricEntity(1, "Rubric", skillId, null);
        final SkillRubric model = SkillRubric.builder().id(1).title("Rubric").skillId(skillId).build();

        when(skillRubricJPARepository.findBySkillId(skillId)).thenReturn(List.of(entity));
        when(skillRubricMapper.toModelList(List.of(entity))).thenReturn(List.of(model));

        final List<SkillRubric> result = skillRubricRepository.findBySkillId(skillId);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void findById_shouldReturnRubricWhenFoundAndActive() {
        final SkillRubricEntity entity = new SkillRubricEntity(1, "Rubric", 10, null);
        final SkillRubric model = SkillRubric.builder().id(1).title("Rubric").skillId(10).build();

        when(skillRubricJPARepository.findByIdAndDeletionDateIsNull(1)).thenReturn(Optional.of(entity));
        when(skillRubricMapper.toModel(entity)).thenReturn(model);

        final Optional<SkillRubric> result = skillRubricRepository.findById(1);

        assertTrue(result.isPresent());
    }

    @Test
    void findById_shouldReturnEmptyWhenNotFound() {
        when(skillRubricJPARepository.findByIdAndDeletionDateIsNull(1)).thenReturn(Optional.empty());

        final Optional<SkillRubric> result = skillRubricRepository.findById(1);

        assertFalse(result.isPresent());
    }

    @Test
    void save_shouldMapAndSave() {
        final SkillRubric rubric = SkillRubric.builder().title("New").skillId(10).build();
        final SkillRubricEntity entity = new SkillRubricEntity();
        final SkillRubricEntity saved = new SkillRubricEntity(1, "New", 10, null);
        final SkillRubric savedModel = SkillRubric.builder().id(1).title("New").skillId(10).build();

        when(skillRubricMapper.toEntity(rubric)).thenReturn(entity);
        when(skillRubricJPARepository.save(entity)).thenReturn(saved);
        when(skillRubricMapper.toModel(saved)).thenReturn(savedModel);

        final SkillRubric result = skillRubricRepository.save(rubric);

        assertNotNull(result);
        assertEquals(1, result.getId());
    }

    @Test
    void softDeleteById_shouldDelegate() {
        skillRubricRepository.softDeleteById(1);
        verify(skillRubricJPARepository).softDeleteById(1);
    }

    @Test
    void softDeleteBySkillId_shouldDelegate() {
        skillRubricRepository.softDeleteBySkillId(10);
        verify(skillRubricJPARepository).softDeleteBySkillId(10);
    }

    @Test
    void findActiveIdsBySkillId_shouldReturnIds() {
        when(skillRubricJPARepository.findActiveIdsBySkillId(10)).thenReturn(Arrays.asList(1, 2));

        final List<Integer> result = skillRubricRepository.findActiveIdsBySkillId(10);

        assertEquals(2, result.size());
    }
}

