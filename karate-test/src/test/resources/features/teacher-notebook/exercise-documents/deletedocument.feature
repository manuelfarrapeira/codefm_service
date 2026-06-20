@Regresion
Feature: Delete Exercise Document

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Delete document - Document not found
    Given path '/teacher-notebook/v1/exercises/1/documents/99999'
    When method DELETE
    Then status 404
    And match response.code == "1003"

  Scenario: Delete document - Exercise not found
    Given path '/teacher-notebook/v1/exercises/99999/documents/1'
    When method DELETE
    Then status 404
    And match response.code == "1003"

