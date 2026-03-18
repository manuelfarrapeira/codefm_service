package org.web.codefm.domain.usecase.teachernotebook;

import org.web.codefm.domain.entity.teachernotebook.ClassRubric;
import org.web.codefm.domain.entity.teachernotebook.StudentClassRubricCriteria;
import org.web.codefm.domain.entity.teachernotebook.StudentCriteriaGroup;

import java.util.List;

/**
 * Interface that defines class rubric assignment and student criterion operations for teachers.
 * Handles assignment of rubrics to classes and criteria to students.
 */
public interface ClassRubricUseCase {

    /**
     * Retrieves all rubrics assigned to a class, enriched with skill ID and active criteria list.
     *
     * @param classId The unique identifier of the class
     * @return List of ClassRubric assignments with rubricTitle, skillId and criteria populated
     */
    List<ClassRubric> getRubricsByClassId(Integer classId);

    /**
     * Assigns a rubric to a class.
     *
     * @param classId  The unique identifier of the class
     * @param rubricId The unique identifier of the rubric to assign
     * @return The created ClassRubric assignment
     */
    ClassRubric assignRubricToClass(Integer classId, Integer rubricId);

    /**
     * Removes a class-rubric assignment and cascades soft-delete to student criterion assignments.
     *
     * @param classRubricId The unique identifier of the class-rubric assignment to remove
     */
    void removeRubricFromClass(Integer classRubricId);

    /**
     * Retrieves all criterion assignments for all students in a class,
     * grouped by student with nested rubric and criterion information.
     *
     * @param classId The unique identifier of the class
     * @return List of StudentCriteriaGroup with student, rubric and criterion data
     */
    List<StudentCriteriaGroup> getAllStudentCriteriaByClassId(Integer classId);

    /**
     * Retrieves all criterion assignments for a specific student in a class,
     * grouped by student with nested rubric and criterion information.
     *
     * @param classId   The unique identifier of the class
     * @param studentId The unique identifier of the student
     * @return List of StudentCriteriaGroup with student, rubric and criterion data
     */
    List<StudentCriteriaGroup> getStudentCriteriaByClassAndStudent(Integer classId, Integer studentId);

    /**
     * Assigns a rubric criterion to a student within a class-rubric assignment.
     *
     * @param classRubricId The unique identifier of the class-rubric assignment
     * @param studentId     The unique identifier of the student
     * @param criterionId   The unique identifier of the criterion to assign
     * @return The created StudentClassRubricCriteria assignment
     */
    StudentClassRubricCriteria assignCriterionToStudent(Integer classRubricId, Integer studentId, Integer criterionId);

    /**
     * Updates the criterion assigned to a student.
     *
     * @param id          The unique identifier of the existing assignment to update
     * @param criterionId The unique identifier of the new criterion
     * @return The updated StudentClassRubricCriteria assignment
     */
    StudentClassRubricCriteria updateStudentCriterion(Integer id, Integer criterionId);

    /**
     * Soft-deletes a student criterion assignment.
     *
     * @param id The unique identifier of the assignment to remove
     */
    void removeStudentCriterion(Integer id);
}

