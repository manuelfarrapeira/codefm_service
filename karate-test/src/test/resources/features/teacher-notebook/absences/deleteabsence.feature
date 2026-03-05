@Regresion
Feature: Teacher Notebook - Delete Student Absence by ID

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Delete absence - Not found
    Given path '/teacher-notebook/v1/absences/999999'
    When method DELETE
    Then status 404
    And match response.code == "1003"
    And match response.description == "RESOURCE_NOT_FOUND"
