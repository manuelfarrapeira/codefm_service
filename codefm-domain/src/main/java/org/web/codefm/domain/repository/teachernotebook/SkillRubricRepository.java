package org.web.codefm.domain.repository.teachernotebook;

import org.web.codefm.domain.entity.teachernotebook.SkillRubric;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for skill rubric data access operations.
 * Provides methods to retrieve and manage skill rubric information.
 */
public interface SkillRubricRepository {

    /**
     * Finds all active rubrics associated with a specific skill.
     *
     * @param skillId The unique identifier of the skill
     * @return List of active rubrics belonging to the specified skill
     */
    List<SkillRubric> findBySkillId(Integer skillId);

    /**
     * Finds a rubric by its ID that has not been soft-deleted.
     *
     * @param rubricId The unique identifier of the rubric
     * @return An Optional containing the SkillRubric if found and active, or empty otherwise
     */
    Optional<SkillRubric> findById(Integer rubricId);

    /**
     * Saves a new rubric or updates an existing one.
     *
     * @param rubric The rubric object to save
     * @return The saved rubric object with generated ID
     */
    SkillRubric save(SkillRubric rubric);

    /**
     * Soft-deletes a rubric by setting its deletion date.
     *
     * @param rubricId The ID of the rubric to soft-delete
     */
    void softDeleteById(Integer rubricId);

    /**
     * Soft-deletes all rubrics associated with a specific skill.
     *
     * @param skillId The ID of the skill whose rubrics should be soft-deleted
     */
    void softDeleteBySkillId(Integer skillId);

    /**
     * Finds all active rubric IDs associated with a specific skill.
     *
     * @param skillId The unique identifier of the skill
     * @return List of active rubric IDs belonging to the specified skill
     */
    List<Integer> findActiveIdsBySkillId(Integer skillId);
}

