import com.intuit.karate.junit5.Karate;
import jakarta.annotation.Generated;


@Generated("karate-test")
public class KarateTestRunner {

    @Karate.Test
    Karate runAccesoAutenticadoTests() {
        return Karate.run("classpath:features").tags("@Regresion").relativeTo(getClass());
    }

}