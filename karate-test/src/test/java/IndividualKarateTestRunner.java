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

}
