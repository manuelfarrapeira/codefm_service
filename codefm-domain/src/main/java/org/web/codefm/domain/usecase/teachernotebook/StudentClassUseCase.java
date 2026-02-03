package org.web.codefm.domain.usecase.teachernotebook;

/**
 * Interface that defines student-class association operations.
 * Handles adding and removing students from classes.
 */
public interface StudentClassUseCase {

    /**
     * Associates a student with a class.
     *
     * @param classId   The unique identifier of the class
     * @param studentId The unique identifier of the student
     */
    void addStudentToClass(Integer classId, Integer studentId);

    /**
     * Removes a student from a class.
     *
     * @param classId   The unique identifier of the class
     * @param studentId The unique identifier of the student
     */
    void removeStudentFromClass(Integer classId, Integer studentId);
}

