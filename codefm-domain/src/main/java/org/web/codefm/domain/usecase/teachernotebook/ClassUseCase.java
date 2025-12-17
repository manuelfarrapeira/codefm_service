package org.web.codefm.domain.usecase.teachernotebook;

import java.util.List;

import org.web.codefm.domain.entity.teachernotebook.Class;

/**
 * Interface that defines class operations for teachers.
 * Handles class data retrieval for a specific school.
 */
public interface ClassUseCase {

    /**
     * Retrieves all active classes for a specific school owned by the authenticated teacher.
     *
     * @param schoolId The unique identifier of the school
     * @return List of active classes belonging to the specified school
     */
    List<Class> getClassesBySchoolId(Integer schoolId);

    /**
     * Creates a new class for a specific school owned by the authenticated teacher.
     *
     * @param clazz The class data to create
     * @return The created class with generated ID
     */
    Class createClass(Class clazz);

    /**
     * Soft-deletes a class for the authenticated teacher.
     * Retrieves the teacher ID from the session and delegates to the service.
     *
     * @param classId The ID of the class to soft-delete
     * @throws org.web.codefm.domain.exception.teachernotebook.ClassNotFoundException   if the class does not exist
     * @throws org.web.codefm.domain.exception.teachernotebook.SchoolForbiddenException if the teacher does not own the school
     */
    void softDeleteClass(Integer classId);

  /**
   * Updates an existing class for the authenticated teacher. Retrieves the teacher ID from the session and delegates to the service.
   *
   * @param classId The ID of the class to update
   * @param clazz The class data with updated fields
   * @return The updated class
   * @throws org.web.codefm.domain.exception.teachernotebook.ClassNotFoundException if the class does not exist
   * @throws org.web.codefm.domain.exception.teachernotebook.ClassForbiddenException if the teacher does not own the school
   * @throws org.web.codefm.domain.exception.teachernotebook.ClassValidationException if validation fails
   */
  Class updateClass(Integer classId, Class clazz);
}

