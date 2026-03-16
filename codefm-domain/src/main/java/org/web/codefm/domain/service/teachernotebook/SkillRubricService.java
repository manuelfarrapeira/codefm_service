package org.web.codefm.domain.service.teachernotebook;

import org.web.codefm.domain.entity.teachernotebook.SkillRubric;
import org.web.codefm.domain.entity.teachernotebook.SkillRubricCriteria;

import java.util.List;

/**
 * Service interface for skill rubric and criteria business logic operations.
 * Acts as an intermediary between use cases and repositories.
 */
public interface SkillRubricService {

    /**
     * Retrieves all active rubrics for a specific skill owned by the authenticated teacher.
     *
     * @param skillId The unique identifier of the skill
     * @return List of rubrics with their criteria belonging to the specified skill
     */
    List<SkillRubric> getRubricsBySkillId(Integer skillId);

    /**
     * Creates a new rubric (title only) for a specific skill.
     *
     * @param skillId The ID of the skill to associate with
     * @param rubric  The rubric object containing title
     * @return The created rubric with generated ID
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
     * Soft-deletes a rubric and hard-deletes all its criteria.
     *
     * @param rubricId The ID of the rubric to delete
     */
    void deleteRubric(Integer rubricId);

    /**
     * Retrieves all active criteria for a specific rubric.
     *
     * @param rubricId The unique identifier of the rubric
     * @return List of active criteria belonging to the specified rubric
     */
    List<SkillRubricCriteria> getCriteriaByRubricId(Integer rubricId);

    /**
     * Creates a single criterion for a rubric. Validates that the grade range
     * does not overlap with any existing active criterion.
     *
     * @param rubricId  The ID of the rubric
     * @param criterion The criterion to create
     * @return The created criterion with generated ID
     */
    SkillRubricCriteria createCriterion(Integer rubricId, SkillRubricCriteria criterion);

    /**
     * Updates an existing criterion. Validates that the new grade range
     * does not overlap with any other active criterion of the same rubric.
     *
     * @param rubricId    The ID of the rubric (for ownership validation)
     * @param criterionId The ID of the criterion to update
     * @param criterion   The criterion data to update
     * @return The updated criterion
     */
    SkillRubricCriteria updateCriterion(Integer rubricId, Integer criterionId, SkillRubricCriteria criterion);

    /**
     * Soft-deletes a single criterion.
     *
     * @param rubricId    The ID of the rubric (for ownership validation)
     * @param criterionId The ID of the criterion to soft-delete
     */
    void deleteCriterion(Integer rubricId, Integer criterionId);
}

