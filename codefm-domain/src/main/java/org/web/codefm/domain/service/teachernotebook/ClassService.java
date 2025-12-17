package org.web.codefm.domain.service.teachernotebook;

import java.util.List;
import java.util.Optional;

import org.web.codefm.domain.entity.teachernotebook.Class;

/**
 * Service interface for class business logic operations.
 * Acts as an intermediary between use cases and repositories.
 */
public interface ClassService {

    /**
     * Retrieves all active classes for a specific school, verifying teacher ownership.
     *
     * @param schoolId  The unique identifier of the school
     * @param teacherId The unique identifier of the teacher
     * @return List of active classes belonging to the specified school
     */
    List<Class> getActiveClassesBySchoolIdAndTeacherId(Integer schoolId, Integer teacherId);

    /**
     * Creates a new class for a specific school, validating data and teacher ownership.
     *
     * @param clazz     The class data to create
     * @param teacherId The unique identifier of the teacher
     * @return The created class with generated ID
     */
    Class createClass(Class clazz, Integer teacherId);

    /**
     * Retrieves a class by its ID.
     *
     * @param classId The unique identifier of the class
     * @return An Optional containing the Class if found, or empty otherwise
     */
    Optional<Class> getClassById(Integer classId);

    /**
     * Soft-deletes a class by setting its deletion date.
     * Validates that the class exists and belongs to a school owned by the specified teacher.
     *
     * @param classId   The ID of the class to soft-delete
     * @param teacherId The ID of the teacher attempting the soft-delete
     * @throws org.web.codefm.domain.exception.teachernotebook.ClassNotFoundException   if the class does not exist
     * @throws org.web.codefm.domain.exception.teachernotebook.SchoolForbiddenException if the teacher does not own the school
     */
    void softDeleteClass(Integer classId, Integer teacherId);

  /**
   * Updates an existing class. Validates that the class exists, belongs to a school owned by the specified teacher, and that the new data
   * is valid.
   *
   * @param classId The ID of the class to update
   * @param clazz The class data with updated fields
   * @param teacherId The ID of the teacher attempting the update
   * @return The updated class
   * @throws org.web.codefm.domain.exception.teachernotebook.ClassNotFoundException if the class does not exist
   * @throws org.web.codefm.domain.exception.teachernotebook.ClassForbiddenException if the teacher does not own the school
   * @throws org.web.codefm.domain.exception.teachernotebook.ClassValidationException if validation fails
   */
  Class updateClass(Integer classId, Class clazz, Integer teacherId);
}

