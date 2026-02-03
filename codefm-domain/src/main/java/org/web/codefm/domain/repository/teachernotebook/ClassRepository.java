package org.web.codefm.domain.repository.teachernotebook;

import org.web.codefm.domain.entity.teachernotebook.Class;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for class data access operations.
 * Provides methods to retrieve and manage class information.
 */
public interface ClassRepository {

    /**
     * Finds all active classes (not soft-deleted) for a specific school.
     *
     * @param schoolId  The unique identifier of the school
     * @param teacherId The unique identifier of the teacher (to verify ownership)
     * @return List of active classes belonging to the specified school
     */
    List<Class> findActiveClassesBySchoolIdAndTeacherId(Integer schoolId, Integer teacherId);

    /**
     * Saves a new class to the database.
     *
     * @param clazz The class data to save
     * @return The saved class with generated ID
     */
    Class save(Class clazz);

    /**
     * Finds a class by its ID.
     *
     * @param classId The unique identifier of the class
     * @return An Optional containing the Class if found, or empty otherwise
     */
    Optional<Class> findById(Integer classId);

    /**
     * Finds an active class (not soft-deleted) by ID and teacher ID.
     *
     * @param classId   The unique identifier of the class
     * @param teacherId The unique identifier of the teacher (to verify ownership)
     * @return An Optional containing the Class if found and active, or empty otherwise
     */
    Optional<Class> findByIdAndTeacherIdAndDeletionDateIsNull(Integer classId, Integer teacherId);

    /**
     * Soft-deletes a class by setting its deletion date.
     *
     * @param classId   The ID of the class to soft-delete
     * @param teacherId The ID of the teacher attempting the soft-delete
     * @return The soft-deleted Class object
     */
    Class softDeleteClass(Integer classId, Integer teacherId);
}

