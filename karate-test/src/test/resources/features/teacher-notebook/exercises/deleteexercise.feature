@Regresion
Feature: Delete Exercise

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Delete exercise - Not found
    Given path '/teacher-notebook/v1/exercises/9999'
    When method DELETE
    Then status 404
    And match response.code == "1003"
    And match response.description == "RESOURCE_NOT_FOUND"

