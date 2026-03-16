package org.web.codefm.domain.repository.teachernotebook;

import org.web.codefm.domain.entity.teachernotebook.SkillRubricCriteria;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for skill rubric criteria data access operations.
 * Provides methods to retrieve and manage rubric evaluation criteria.
 */
public interface SkillRubricCriteriaRepository {

    /**
     * Finds all active criteria associated with a specific rubric.
     *
     * @param rubricId The unique identifier of the rubric
     * @return List of active criteria belonging to the specified rubric
     */
    List<SkillRubricCriteria> findActiveByRubricId(Integer rubricId);

    /**
     * Finds an active criterion by its ID.
     *
     * @param criterionId The unique identifier of the criterion
     * @return An Optional containing the criterion if found and active
     */
    Optional<SkillRubricCriteria> findActiveById(Integer criterionId);

    /**
     * Saves a new criterion or updates an existing one.
     *
     * @param criteria The criterion to save
     * @return The saved criterion with generated ID
     */
    SkillRubricCriteria save(SkillRubricCriteria criteria);

    /**
     * Soft-deletes a criterion by setting its deletion date.
     *
     * @param criterionId The ID of the criterion to soft-delete
     */
    void softDeleteById(Integer criterionId);

    /**
     * Soft-deletes all criteria associated with a specific rubric.
     *
     * @param rubricId The ID of the rubric whose criteria should be soft-deleted
     */
    void softDeleteByRubricId(Integer rubricId);

    /**
     * Soft-deletes all criteria associated with the given rubric IDs.
     *
     * @param rubricIds The list of rubric IDs whose criteria should be soft-deleted
     */
    void softDeleteByRubricIds(List<Integer> rubricIds);
}

