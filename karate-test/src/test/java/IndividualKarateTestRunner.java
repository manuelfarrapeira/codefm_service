import com.intuit.karate.junit5.Karate;

public class IndividualKarateTestRunner {

	@Karate.Test
	Karate runAll() {
		return Karate.run().relativeTo(getClass());
	}

	@Karate.Test
	Karate testLogin() {
		return Karate.run("features/login").relativeTo(getClass());
	}

	@Karate.Test
	Karate testGreeting() {
		return Karate.run("features/greeting/greeting").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookGetSchools() {
		return Karate.run("features/teacher-notebook/schools/getschools").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookPutSchools() {
		return Karate.run("features/teacher-notebook/schools/createschool").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookDeleteSchool() {
		return Karate.run("features/teacher-notebook/schools/deleteschool").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookUpdateSchool() {
		return Karate.run("features/teacher-notebook/schools/updateschool").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookGetClasses() {
		return Karate.run("features/teacher-notebook/classes/getclasses").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookDeleteClasses() {
		return Karate.run("features/teacher-notebook/classes/deleteclass").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookUpdateClasses() {
		return Karate.run("features/teacher-notebook/classes/updateclass").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookCreateClasses() {
		return Karate.run("features/teacher-notebook/classes/createclass").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookSearchStudent() {
		return Karate.run("features/teacher-notebook/students/searchstudents").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookCreateStudent() {
		return Karate.run("features/teacher-notebook/students/createstudent").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookUpdateStudent() {
		return Karate.run("features/teacher-notebook/students/updatestudent").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookDeleteStudent() {
		return Karate.run("features/teacher-notebook/students/deletestudent").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookUploadPhoto() {
		return Karate.run("features/teacher-notebook/students/uploadphoto").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookGetAllStudents() {
		return Karate.run("features/teacher-notebook/students/getallstudents").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookAddStudentToClass() {
		return Karate.run("features/teacher-notebook/classes/addstudenttoclass").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookDEletePhoto() {
		return Karate.run("features/teacher-notebook/students/deletephoto").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookGetPhoto() {
		return Karate.run("features/teacher-notebook/students/downloadphoto").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookGetSubjects() {
		return Karate.run("features/teacher-notebook/subjects/getsubjects").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookCreateSubjects() {
		return Karate.run("features/teacher-notebook/subjects/createsubject").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookDeleteSubjects() {
		return Karate.run("features/teacher-notebook/subjects/deletesubject").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookUpdateSubjects() {
		return Karate.run("features/teacher-notebook/subjects/updatesubject").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookSchedules() {
		return Karate.run("features/teacher-notebook/schedules/schedules").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookGetSubjectsByClass() {
		return Karate.run("features/teacher-notebook/subject-classes/getsubjectsbyclass").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookGetClassesWithSubjects() {
		return Karate.run("features/teacher-notebook/subject-classes/getclasseswithsubjects").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookAssignSubjectsToClass() {
		return Karate.run("features/teacher-notebook/subject-classes/assignsubjectstoclass").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookRemoveSubjectsFromClass() {
		return Karate.run("features/teacher-notebook/subject-classes/removesubjectsfromclass").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookGetExercises() {
		return Karate.run("features/teacher-notebook/exercises/getexercises").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookCreateExercise() {
		return Karate.run("features/teacher-notebook/exercises/createexercise").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookUpdateExercise() {
		return Karate.run("features/teacher-notebook/exercises/updateexercise").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookDeleteExercise() {
		return Karate.run("features/teacher-notebook/exercises/deleteexercise").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookUploadExerciseDocument() {
		return Karate.run("features/teacher-notebook/exercise-documents/uploaddocument").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookDownloadExerciseDocument() {
		return Karate.run("features/teacher-notebook/exercise-documents/downloaddocument").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookUpdateExerciseDocument() {
		return Karate.run("features/teacher-notebook/exercise-documents/updatedocument").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookDeleteExerciseDocument() {
		return Karate.run("features/teacher-notebook/exercise-documents/deletedocument").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookGetGradesByClass() {
		return Karate.run("features/teacher-notebook/grades/getgradesbyclass").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookGetGradesByStudent() {
		return Karate.run("features/teacher-notebook/grades/getgradesbystudent").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookCreateGrade() {
		return Karate.run("features/teacher-notebook/grades/creategrade").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookUpdateGrade() {
		return Karate.run("features/teacher-notebook/grades/updategrade").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookDeleteGrade() {
		return Karate.run("features/teacher-notebook/grades/deletegrade").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookGetCalendarAlerts() {
		return Karate.run("features/teacher-notebook/calendar-alerts/getcalendaralerts").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookCreateCalendarAlert() {
		return Karate.run("features/teacher-notebook/calendar-alerts/createcalendaralert").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookUpdateCalendarAlert() {
		return Karate.run("features/teacher-notebook/calendar-alerts/updatecalendaralert").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookDeleteCalendarAlert() {
		return Karate.run("features/teacher-notebook/calendar-alerts/deletecalendaralert").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookGetCalendarAlertsByMonth() {
		return Karate.run("features/teacher-notebook/calendar-alerts/getcalendaralertsbymonth").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookExportGrades() {
		return Karate.run("features/teacher-notebook/grades/exportgrades").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookCreateAbsences() {
		return Karate.run("features/teacher-notebook/absences/createabsences").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookGetAbsences() {
		return Karate.run("features/teacher-notebook/absences/getabsences").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookDeleteAbsence() {
		return Karate.run("features/teacher-notebook/absences/deleteabsence").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookDeleteAbsencesByStudentAndDate() {
		return Karate.run("features/teacher-notebook/absences/deleteabsencesbystudentanddate").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookGetSkills() {
		return Karate.run("features/teacher-notebook/skills/getskills").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookCreateSkill() {
		return Karate.run("features/teacher-notebook/skills/createskill").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookUpdateSkill() {
		return Karate.run("features/teacher-notebook/skills/updateskill").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookDeleteSkill() {
		return Karate.run("features/teacher-notebook/skills/deleteskill").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookGetSkillRubrics() {
		return Karate.run("features/teacher-notebook/skill-rubrics/getrubrics").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookCreateSkillRubric() {
		return Karate.run("features/teacher-notebook/skill-rubrics/createrubric").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookUpdateSkillRubric() {
		return Karate.run("features/teacher-notebook/skill-rubrics/updaterubric").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookDeleteSkillRubric() {
		return Karate.run("features/teacher-notebook/skill-rubrics/deleterubric").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookGetSkillRubricCriteria() {
		return Karate.run("features/teacher-notebook/skill-rubrics/getcriteria").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookCreateSkillRubricCriteria() {
		return Karate.run("features/teacher-notebook/skill-rubrics/createcriteria").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookUpdateSkillRubricCriteria() {
		return Karate.run("features/teacher-notebook/skill-rubrics/updatecriteria").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookDeleteSkillRubricCriteria() {
		return Karate.run("features/teacher-notebook/skill-rubrics/deletecriteria").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookGetClassRubrics() {
		return Karate.run("features/teacher-notebook/class-rubrics/getrubricsbyclass").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookAssignRubricToClass() {
		return Karate.run("features/teacher-notebook/class-rubrics/assignrubrictoclass").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookRemoveRubricFromClass() {
		return Karate.run("features/teacher-notebook/class-rubrics/removerubricfromclass").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookGetAllStudentCriteria() {
		return Karate.run("features/teacher-notebook/class-rubrics/getallstudentcriteria").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookGetStudentCriteria() {
		return Karate.run("features/teacher-notebook/class-rubrics/getstudentcriteria").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookAssignCriterionToStudent() {
		return Karate.run("features/teacher-notebook/class-rubrics/assigncriterion").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookUpdateStudentCriterion() {
		return Karate.run("features/teacher-notebook/class-rubrics/updatestudentcriterion").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookRemoveStudentCriterion() {
		return Karate.run("features/teacher-notebook/class-rubrics/removestudentcriterion").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookUploadGradeDocument() {
		return Karate.run("features/teacher-notebook/grade-documents/uploaddocument").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookDownloadGradeDocument() {
		return Karate.run("features/teacher-notebook/grade-documents/downloaddocument").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookUpdateGradeDocument() {
		return Karate.run("features/teacher-notebook/grade-documents/updatedocument").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookDeleteGradeDocument() {
		return Karate.run("features/teacher-notebook/grade-documents/deletedocument").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookGetStudentGroups() {
		return Karate.run("features/teacher-notebook/students/getstudentgroups").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookGetSavedStudentGroups() {
		return Karate.run("features/teacher-notebook/saved-student-groups/getsavedstudentgroups").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookCreateSavedStudentGroups() {
		return Karate.run("features/teacher-notebook/saved-student-groups/createsavedstudentgroups").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookUpdateSavedStudentGroups() {
		return Karate.run("features/teacher-notebook/saved-student-groups/updatesavedstudentgroups").relativeTo(getClass());
	}

	@Karate.Test
	Karate testTeacherNotebookDeleteSavedStudentGroups() {
		return Karate.run("features/teacher-notebook/saved-student-groups/deletesavedstudentgroup").relativeTo(getClass());
	}

}
