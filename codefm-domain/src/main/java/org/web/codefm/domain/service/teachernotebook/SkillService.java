package org.web.codefm.domain.service.teachernotebook;

import org.web.codefm.domain.entity.teachernotebook.Skill;

import java.util.List;

/**
 * Service interface for skill business logic operations.
 * Acts as an intermediary between use cases and repositories.
 */
public interface SkillService {

    /**
     * Retrieves all skills associated with the authenticated teacher.
     *
     * @return List of skills belonging to the authenticated teacher
     */
    List<Skill> getSkillsByTeacher();

    /**
     * Creates a new skill for the authenticated teacher.
     *
     * @param skill The skill object to create.
     * @return The created skill object.
     */
    Skill createSkill(Skill skill);

    /**
     * Updates an existing skill.
     *
     * @param skillId The ID of the skill to update.
     * @param skill   The skill object containing the updated data.
     * @return The updated skill object.
     */
    Skill updateSkill(Integer skillId, Skill skill);

    /**
     * Soft-deletes a skill by setting its deletion date.
     *
     * @param skillId The ID of the skill to soft-delete.
     */
    void softDeleteSkill(Integer skillId);
}

