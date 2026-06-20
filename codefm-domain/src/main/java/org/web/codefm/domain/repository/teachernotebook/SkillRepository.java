package org.web.codefm.domain.repository.teachernotebook;

import org.web.codefm.domain.entity.teachernotebook.Skill;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for skill data access operations.
 * Provides methods to retrieve and manage skill information.
 */
public interface SkillRepository {

    /**
     * Finds all skills associated with a specific teacher.
     *
     * @param teacherId The unique identifier of the teacher
     * @return List of skills belonging to the specified teacher
     */
    List<Skill> findByTeacherId(Integer teacherId);

    /**
     * Saves a new skill or updates an existing one.
     *
     * @param skill The skill object to save.
     * @return The saved skill object, typically with the generated ID.
     */
    Skill save(Skill skill);

    /**
     * Finds a skill by its ID.
     *
     * @param skillId The unique identifier of the skill.
     * @return An Optional containing the Skill if found, or empty otherwise.
     */
    Optional<Skill> findById(Integer skillId);

    /**
     * Finds a skill by its ID and teacher ID.
     *
     * @param skillId   The unique identifier of the skill.
     * @param teacherId The unique identifier of the teacher.
     * @return An Optional containing the Skill if found and owned by the teacher, or empty otherwise.
     */
    Optional<Skill> findByIdAndTeacherId(Integer skillId, Integer teacherId);

    /**
     * Soft-deletes a skill by setting its deletion date.
     *
     * @param skillId   The ID of the skill to soft-delete.
     * @param teacherId The ID of the teacher attempting the soft-delete.
     * @return The soft-deleted Skill object.
     */
    Skill softDeleteSkill(Integer skillId, Integer teacherId);
}

