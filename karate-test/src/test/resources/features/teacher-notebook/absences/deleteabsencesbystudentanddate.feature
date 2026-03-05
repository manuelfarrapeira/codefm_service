@Regresion
Feature: Teacher Notebook - Delete Student Absences by Student and Date

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Delete absences by student, class and date
    Given path '/teacher-notebook/v1/classes/1/absences'
    And param studentId = 1
    And param date = '05/03/2026'
    When method DELETE
    Then status 204

  Scenario: Delete absences - Class not found
    Given path '/teacher-notebook/v1/classes/9999/absences'
    And param studentId = 1
    And param date = '05/03/2026'
    When method DELETE
    Then status 404
    And match response.code == "1003"
    And match response.description == "RESOURCE_NOT_FOUND"
