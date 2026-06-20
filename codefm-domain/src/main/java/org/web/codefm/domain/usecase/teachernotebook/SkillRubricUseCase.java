package org.web.codefm.domain.usecase.teachernotebook;

import org.web.codefm.domain.entity.teachernotebook.SkillRubric;
import org.web.codefm.domain.entity.teachernotebook.SkillRubricCriteria;

import java.util.List;

/**
 * Interface that defines skill rubric and criteria operations for teachers.
 * Handles rubric and criteria data retrieval and management.
 */
public interface SkillRubricUseCase {

    /**
     * Retrieves all active rubrics for a specific skill.
     *
     * @param skillId The unique identifier of the skill
     * @return List of rubrics with their criteria
     */
    List<SkillRubric> getRubricsBySkillId(Integer skillId);

    /**
     * Creates a new rubric (title only) for a specific skill.
     *
     * @param skillId The ID of the skill
     * @param rubric  The rubric object containing title
     * @return The created rubric
     */
    SkillRubric createRubric(Integer skillId, SkillRubric rubric);

    /**
     * Updates the title of an existing rubric.
     *
     * @param rubricId The ID of the rubric to update
     * @param rubric   The rubric object containing updated title
     * @return The updated rubric
     */
    SkillRubric updateRubric(Integer rubricId, SkillRubric rubric);

    /**
     * Soft-deletes a rubric and hard-deletes its criteria.
     *
     * @param rubricId The ID of the rubric to delete
     */
    void deleteRubric(Integer rubricId);

    /**
     * Retrieves all active criteria for a specific rubric.
     *
     * @param rubricId The unique identifier of the rubric
     * @return List of active criteria
     */
    List<SkillRubricCriteria> getCriteriaByRubricId(Integer rubricId);

    /**
     * Creates a single criterion for a rubric.
     *
     * @param rubricId  The ID of the rubric
     * @param criterion The criterion to create
     * @return The created criterion
     */
    SkillRubricCriteria createCriterion(Integer rubricId, SkillRubricCriteria criterion);

    /**
     * Updates an existing criterion.
     *
     * @param rubricId    The ID of the rubric
     * @param criterionId The ID of the criterion to update
     * @param criterion   The criterion data
     * @return The updated criterion
     */
    SkillRubricCriteria updateCriterion(Integer rubricId, Integer criterionId, SkillRubricCriteria criterion);

    /**
     * Soft-deletes a single criterion.
     *
     * @param rubricId    The ID of the rubric
     * @param criterionId The ID of the criterion
     */
    void deleteCriterion(Integer rubricId, Integer criterionId);
}

