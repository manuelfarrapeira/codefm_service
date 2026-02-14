# Gatling Stress Tests

Este módulo contiene tests de estrés para los endpoints de CodeFM usando Gatling.

## Requisitos

- Java 17+
- Maven 3.8+
- La aplicación CodeFM debe estar corriendo

## Configuración

### Variables de Sistema

| Variable    | Descripción                | Valor por defecto       |
|-------------|----------------------------|-------------------------|
| `baseUrl`   | URL base de la API         | `http://localhost:8081` |
| `authToken` | Token JWT de autenticación | `your-jwt-token-here`   |

## Ejecución

### Ejecutar todos los tests de estrés

```bash
cd gatling-test
mvn gatling:test
```

### Ejecutar con configuración personalizada

```bash
mvn gatling:test -DbaseUrl=http://localhost:8081 -DauthToken=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Ejecutar una simulación específica

```bash
mvn gatling:test -Dgatling.simulationClass=org.web.codefm.gatling.ClassesStressSimulation
```

## Simulaciones Disponibles

### ClassesStressSimulation

Tests de estrés sobre el endpoint `/teacher-notebook/v1/school/{schoolId}/classes`:

**Escenarios:**

- `Get Classes by School`: Obtiene clases de un colegio
- `Create Class`: Crea nuevas clases
- `Mixed Operations`: Operaciones combinadas

**Perfil de carga:**

1. Espera inicial: 5 segundos
2. Rampa de usuarios: 0 → 50 usuarios en 30 segundos
3. Carga constante: 20 usuarios/segundo durante 1 minuto
4. Incremento: 20 → 50 usuarios/segundo en 30 segundos
5. Carga máxima: 50 usuarios/segundo durante 1 minuto

**Aserciones:**

- Tiempo de respuesta máximo: < 5000ms
- Percentil 95: < 2000ms
- Peticiones exitosas: > 95%
- Peticiones fallidas: < 5%

## Reportes

Los reportes HTML se generan en:

```
gatling-test/target/gatling/
```

Abrir el archivo `index.html` en el navegador para ver:

- Gráficos de tiempos de respuesta
- Distribución de peticiones
- Usuarios activos en el tiempo
- Errores y fallos

## Crear Nueva Simulación

1. Crear clase en `src/test/java/org/web/codefm/gatling/`
2. Extender `Simulation`
3. Definir protocolo HTTP, escenarios y carga

```java
public class MiNuevaSimulation extends Simulation {

    HttpProtocolBuilder httpProtocol = http
            .baseUrl(BASE_URL)
            .header("Authorization", "Bearer " + AUTH_TOKEN);

    ScenarioBuilder miEscenario = scenario("Mi Escenario")
            .exec(http("Mi Request")
                    .get("/mi-endpoint")
                    .check(status().is(200)));

    {
        setUp(
                miEscenario.injectOpen(rampUsers(100).during(Duration.ofSeconds(60)))
        ).protocols(httpProtocol);
    }
}
```

## Integración CI/CD

```yaml
# Ejemplo para GitHub Actions
- name: Run Gatling Tests
  run: |
    cd gatling-test
    mvn gatling:test -DbaseUrl=${{ secrets.API_URL }} -DauthToken=${{ secrets.AUTH_TOKEN }}
```

