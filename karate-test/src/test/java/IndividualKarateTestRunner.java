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


}
