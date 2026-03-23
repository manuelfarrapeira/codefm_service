@Regresion
Feature: Generate Student Groups Endpoint

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Generate student groups successfully
    Given path '/teacher-notebook/v1/classes/4/student-groups'
    When method GET
    Then status 200
    And match response == '#[]'
    And match each response == '#[] #number'

  Scenario: Generate student groups with class not found
    Given path '/teacher-notebook/v1/classes/99999/student-groups'
    When method GET
    Then status 404

