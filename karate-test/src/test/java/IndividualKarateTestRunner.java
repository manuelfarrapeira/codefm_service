import com.intuit.karate.junit5.Karate;


public class IndividualKarateTestRunner {


    @Karate.Test
    Karate testInfisical() {
        return Karate.run("features/infisical").relativeTo(getClass());
    }


    @Karate.Test
    Karate runAll() {
        return Karate.run().relativeTo(getClass());
    }

    @Karate.Test
    Karate testLogin() {
        return Karate.run("features/login").relativeTo(getClass());
    }

    @Karate.Test
    Karate testAuth() {
        return Karate.run("features/auth").relativeTo(getClass());
    }


    @Karate.Test
    Karate testSaludo() {
        return Karate.run("features/saludo/saludo").relativeTo(getClass());
    }

    @Karate.Test
    Karate runAccesoAutenticadoTests() {
        return Karate.run("classpath:features").tags("@Regresion").relativeTo(getClass());
    }


}