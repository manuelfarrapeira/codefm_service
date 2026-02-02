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
  Karate testTeacherNotebooDeleteStudent() {
    return Karate.run("features/teacher-notebook/students/deletestudent").relativeTo(getClass());
  }

  @Karate.Test
  Karate testTeacherNotebooUploadPhoto() {
    return Karate.run("features/teacher-notebook/students/uploadphoto").relativeTo(getClass());
  }

  @Karate.Test
  Karate testTeacherNoteboogetAllStudents() {
    return Karate.run("features/teacher-notebook/students/getallstudents").relativeTo(getClass());
  }

}
