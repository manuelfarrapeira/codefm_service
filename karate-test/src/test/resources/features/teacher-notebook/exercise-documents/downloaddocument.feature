@Regresion
Feature: Download Exercise Document

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Download document - Document not found
    Given path '/teacher-notebook/v1/exercises/1/documents/99999/download'
    When method GET
    Then status 404
    And match response.code == "1003"

  Scenario: Download document - Exercise not found
    Given path '/teacher-notebook/v1/exercises/99999/documents/1/download'
    When method GET
    Then status 404
    And match response.code == "1003"

