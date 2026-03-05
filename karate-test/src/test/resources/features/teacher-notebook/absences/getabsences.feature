@Regresion
Feature: Teacher Notebook - Get Student Absences

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Get absences by class and student
    * def absenceSchema = { id: '#number', studentId: '#number', studentName: '#string', studentSurnames: '#string', classId: '#number', subjectId: '#number', subjectName: '#string', absenceDate: '#string' }
    Given path '/teacher-notebook/v1/classes/1/absences'
    And param studentId = 1
    When method GET
    Then status 200
    And match response == '#array'
    And match each response == absenceSchema

  Scenario: Get absences by class and date
    Given path '/teacher-notebook/v1/classes/1/absences'
    And param date = '05/03/2026'
    When method GET
    Then status 200
    And match response == '#array'

  Scenario: Get absences by class, student and date
    Given path '/teacher-notebook/v1/classes/1/absences'
    And param studentId = 1
    And param date = '05/03/2026'
    When method GET
    Then status 200
    And match response == '#array'

  Scenario: Get absences - Validation error (no studentId and no date)
    Given path '/teacher-notebook/v1/classes/1/absences'
    When method GET
    Then status 400
    And match response.code == "1006"
    And match response.description == "VALIDATION_ERROR"

  Scenario: Get absences - Class not found
    Given path '/teacher-notebook/v1/classes/9999/absences'
    And param studentId = 1
    When method GET
    Then status 404
    And match response.code == "1003"
    And match response.description == "RESOURCE_NOT_FOUND"
