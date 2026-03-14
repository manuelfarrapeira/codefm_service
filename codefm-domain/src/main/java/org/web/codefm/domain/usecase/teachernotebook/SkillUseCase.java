package org.web.codefm.domain.usecase.teachernotebook;

import org.web.codefm.domain.entity.teachernotebook.Skill;

import java.util.List;

/**
 * Interface that defines skill operations for teachers.
 * Handles skill data retrieval and management.
 */
public interface SkillUseCase {

    /**
     * Retrieves all skills associated with the authenticated teacher.
     *
     * @return List of skills belonging to the teacher
     */
    List<Skill> getSkillsByTeacher();

    /**
     * Creates a new skill based on the provided data and assigns it to the authenticated teacher.
     *
     * @param skill The skill object containing the data for the new skill.
     * @return The created skill object.
     */
    Skill createSkill(Skill skill);

    /**
     * Updates an existing skill based on the provided data.
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

