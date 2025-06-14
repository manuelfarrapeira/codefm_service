import com.intuit.karate.junit5.Karate;


public class KarateTestRunner {

    @Karate.Test
    Karate runAccesoAutenticadoTests() {
        return Karate.run("classpath:features").tags("@Regresion").relativeTo(getClass());
    }

}