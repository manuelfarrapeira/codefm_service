package org.web.codefm.domain.service.teachernotebook;

/**
 * Service interface for cascading soft-delete operations across the entity
 * hierarchy. Each method triggers a soft-delete cascade starting from the given
 * entity level downwards.
 */
public interface CascadeSoftDeleteService {

	/**
	 * Cascades soft-delete for all children of the given school: classes →
	 * subjectClasses → exercises → grades/documents, schedules, studentClasses.
	 *
	 * @param schoolId
	 *            The unique identifier of the school.
	 */
	void cascadeDeleteChildrenOfSchool(Integer schoolId);

	/**
	 * Cascades soft-delete for all children of the given class: subjectClasses →
	 * exercises → grades/documents, absences (hard delete), studentClasses,
	 * schedules.
	 *
	 * @param classId
	 *            The unique identifier of the class.
	 */
	void cascadeDeleteChildrenOfClass(Integer classId);

	/**
	 * Cascades soft-delete for all children of the given subject-class association:
	 * exercises → grades/documents, absences (hard delete).
	 *
	 * @param subjectClassId
	 *            The unique identifier of the subject-class association.
	 */
	void cascadeDeleteChildrenOfSubjectClass(Integer subjectClassId);

	/**
	 * Cascades soft-delete for all children of the given subject: subjectClasses →
	 * exercises → grades/documents, schedules.
	 *
	 * @param subjectId
	 *            The unique identifier of the subject.
	 */
	void cascadeDeleteChildrenOfSubject(Integer subjectId);

	/**
	 * Cascades soft-delete for all children of the given exercise: grades and
	 * documents.
	 *
	 * @param exerciseId
	 *            The unique identifier of the exercise.
	 */
	void cascadeDeleteChildrenOfExercise(Integer exerciseId);

	/**
	 * Cascades soft-delete for all children of the given student: exercise grades,
	 * absences (hard delete), and student-class associations.
	 *
	 * @param studentId
	 *            The unique identifier of the student.
	 */
	void cascadeDeleteChildrenOfStudent(Integer studentId);

	/**
	 * Cascades soft-delete for all children of the given student-class association:
	 * exercise grades linked to that student-class entry, absences (hard delete).
	 *
	 * @param studentClassId
	 *            The unique identifier of the student-class association.
	 */
	void cascadeDeleteChildrenOfStudentClass(Integer studentClassId);

	/**
	 * Cascades delete for all children of the given skill: rubrics → criteria.
	 *
	 * @param skillId The unique identifier of the skill.
	 */
	void cascadeDeleteChildrenOfSkill(Integer skillId);

	/**
	 * Cascades delete for all children of the given rubric: criteria (hard delete).
	 *
	 * @param rubricId The unique identifier of the rubric.
	 */
	void cascadeDeleteChildrenOfRubric(Integer rubricId);

    /**
     * Cascades soft-delete for all children of the given class-rubric assignment:
     * student_class_rubric_criteria entries.
     *
     * @param classRubricId The unique identifier of the class-rubric assignment.
     */
    void cascadeDeleteChildrenOfClassRubric(Integer classRubricId);

	/**
	 * Cascades soft-delete for all children of the given skill rubric criterion:
	 * student_class_rubric_criteria entries that reference this criterion.
	 *
	 * @param criterionId The unique identifier of the skill rubric criterion.
	 */
	void cascadeDeleteChildrenOfSkillRubricCriteria(Integer criterionId);
}
