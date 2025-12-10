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



}