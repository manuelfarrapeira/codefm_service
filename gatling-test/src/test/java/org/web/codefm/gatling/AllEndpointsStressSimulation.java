package org.web.codefm.gatling;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.PopulationBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class AllEndpointsStressSimulation extends Simulation {

    private static final String BASE_URL = System.getProperty("baseUrl");
    private static final String USERNAME = System.getProperty("username");
    private static final String PASSWORD = System.getProperty("password");
    private static final String ENDPOINTS = System.getProperty("endpoints", "all");
    private static final int MAX_CONCURRENT_USERS = Integer.parseInt(System.getProperty("maxConcurrentUsers", "50"));

    private static final String accessToken = obtainAccessToken();

    private static String obtainAccessToken() {
        try {
            String credentials = USERNAME + ":" + PASSWORD;
            String basicAuth = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/public/auth/login"))
                    .header("Authorization", basicAuth)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode json = mapper.readTree(response.body());
                String token = json.get("accessToken").asText();
                System.out.println("Login successful. Token obtained.");
                return token;
            } else {
                throw new RuntimeException("Login failed with status: " + response.statusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to obtain access token", e);
        }
    }

    private static List<String> getSelectedEndpoints() {
        if ("all".equalsIgnoreCase(ENDPOINTS)) {
            return Arrays.asList("classes", "students", "schools", "subjects", "subjectsByClass", "schedules");
        }
        return Arrays.asList(ENDPOINTS.split(","));
    }

    HttpProtocolBuilder httpProtocol = http
            .baseUrl(BASE_URL)
            .acceptHeader("application/json")
            .contentTypeHeader("application/json")
            .header("Authorization", "Bearer " + accessToken);

    private ChainBuilder getClassesBySchool() {
        return exec(
                http("GET /school/{schoolId}/classes")
                        .get("/teacher-notebook/v1/school/1/classes")
                        .check(status().is(200))
        );
    }

    private ChainBuilder getAllStudents() {
        return exec(
                http("GET /students/all")
                        .get("/teacher-notebook/v1/students/all")
                        .header("Accept-Language", "es")
                        .check(status().is(200))
        );
    }

    private ChainBuilder getAllSchools() {
        return exec(
                http("GET /schools")
                        .get("/teacher-notebook/v1/schools")
                        .check(status().is(200))
        );
    }

    private ChainBuilder getAllSubjects() {
        return exec(
                http("GET /subjects")
                        .get("/teacher-notebook/v1/subjects")
                        .check(status().is(200))
        );
    }

    private ChainBuilder getSubjectsByClass() {
        return exec(
                http("GET /classes/{classId}/subjects")
                        .get("/teacher-notebook/v1/classes/1/subjects")
                        .check(status().is(200))
        );
    }

    private ChainBuilder getSchedulesByClass() {
        return exec(
                http("GET /classes/{classId}/schedules")
                        .get("/teacher-notebook/v1/classes/1/schedules")
                        .check(status().is(200))
        );
    }

    private ScenarioBuilder createScenario(String name, ChainBuilder chain) {
        return scenario(name + " Stress Test")
                .repeat(10).on(
                        exec(chain)
                                .pause(Duration.ofMillis(50), Duration.ofMillis(200))
                );
    }

    private PopulationBuilder injectUsers(ScenarioBuilder scenario) {
        int lowConcurrent = (int) Math.max(1, MAX_CONCURRENT_USERS * 0.2);
        int midConcurrent = (int) Math.max(2, MAX_CONCURRENT_USERS * 0.5);

        return scenario.injectClosed(
                rampConcurrentUsers(0).to(lowConcurrent).during(Duration.ofSeconds(10)),
                constantConcurrentUsers(lowConcurrent).during(Duration.ofSeconds(15)),
                rampConcurrentUsers(lowConcurrent).to(midConcurrent).during(Duration.ofSeconds(10)),
                constantConcurrentUsers(midConcurrent).during(Duration.ofSeconds(15)),
                rampConcurrentUsers(midConcurrent).to(MAX_CONCURRENT_USERS).during(Duration.ofSeconds(10)),
                constantConcurrentUsers(MAX_CONCURRENT_USERS).during(Duration.ofSeconds(20))
        );
    }

    private PopulationBuilder getPopulationForEndpoint(String endpoint) {
        return switch (endpoint.trim().toLowerCase()) {
            case "classes" -> injectUsers(createScenario("Classes", getClassesBySchool()));
            case "students" -> injectUsers(createScenario("Students", getAllStudents()));
            case "schools" -> injectUsers(createScenario("Schools", getAllSchools()));
            case "subjects" -> injectUsers(createScenario("Subjects", getAllSubjects()));
            case "subjectsbyclass" -> injectUsers(createScenario("SubjectsByClass", getSubjectsByClass()));
            case "schedules" -> injectUsers(createScenario("Schedules", getSchedulesByClass()));
            default -> throw new IllegalArgumentException("Unknown endpoint: " + endpoint);
        };
    }

    {
        List<String> selectedEndpoints = getSelectedEndpoints();
        System.out.println("Running stress tests for endpoints: " + selectedEndpoints);

        List<PopulationBuilder> populations = new ArrayList<>();
        for (String endpoint : selectedEndpoints) {
            populations.add(getPopulationForEndpoint(endpoint));
        }

        PopulationBuilder chainedPopulation = populations.get(0);
        for (int i = 1; i < populations.size(); i++) {
            chainedPopulation = chainedPopulation.andThen(populations.get(i));
        }

        setUp(chainedPopulation)
                .protocols(httpProtocol)
                .assertions(
                        global().responseTime().max().lt(5000),
                        global().responseTime().percentile(95).lt(2000),
                        global().successfulRequests().percent().gt(95.0),
                        global().failedRequests().percent().lt(5.0)
                );
    }
}

