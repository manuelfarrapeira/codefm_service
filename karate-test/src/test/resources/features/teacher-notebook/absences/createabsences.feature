@Regresion
Feature: Teacher Notebook - Create Student Absences

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Create absence for a student with specific subject
    * def absenceSchema = { id: '#number', studentId: '#number', studentName: '#string', studentSurnames: '#string', classId: '#number', subjectId: '#number', subjectName: '#string', absenceDate: '#string' }
    * def requestBody = { studentId: 1, subjectId: 1, date: '03/03/2026' }
    Given path '/teacher-notebook/v1/classes/1/absences'
    And request requestBody
    When method POST
    Then status 201
    And match response == '#array'
    And match each response == absenceSchema
    And match each response contains { studentId: 1, classId: 1, subjectId: 1 }

  Scenario: Create absence without subject - creates for all scheduled subjects of that day
    * def requestBody = { studentId: 8, date: '05/03/2026' }
    Given path '/teacher-notebook/v1/classes/4/absences'
    And request requestBody
    When method POST
    Then status 201
    And match response == '#array'

  Scenario: Create absence - Validation error (invalid date format)
    * def requestBody = { studentId: 1, subjectId: 1, date: '2026-03-15' }
    Given path '/teacher-notebook/v1/classes/1/absences'
    And request requestBody
    When method POST
    Then status 400
    And match response.code == "1006"
    And match response.description == "VALIDATION_ERROR"
    And match response.details contains deep [{ field: 'date', reason: '#string' }]

  Scenario: Create absence - Validation error (missing date)
    * def requestBody = { studentId: 1, subjectId: 1 }
    Given path '/teacher-notebook/v1/classes/1/absences'
    And request requestBody
    When method POST
    Then status 400
    And match response.code == "1006"
    And match response.description == "VALIDATION_ERROR"
    And match response.details contains deep [{ field: 'date', reason: '#string' }]

  Scenario: Create absence - Class not found
    * def requestBody = { studentId: 1, subjectId: 1, date: '05/03/2026' }
    Given path '/teacher-notebook/v1/classes/9999/absences'
    And request requestBody
    When method POST
    Then status 404
    And match response.code == "1003"
    And match response.description == "RESOURCE_NOT_FOUND"

  Scenario: Create absence - Student not found or not owned
    * def requestBody = { studentId: 9999, subjectId: 1, date: '05/03/2026' }
    Given path '/teacher-notebook/v1/classes/1/absences'
    And request requestBody
    When method POST
    Then status 400
    And match response.code == "1006"
    And match response.description == "VALIDATION_ERROR"
    And match response.details contains deep [{ field: 'studentId', reason: '#string' }]

  Scenario: Create absence - Subject not assigned to class
    * def requestBody = { studentId: 1, subjectId: 9999, date: '05/03/2026' }
    Given path '/teacher-notebook/v1/classes/1/absences'
    And request requestBody
    When method POST
    Then status 400
    And match response.code == "1006"
    And match response.description == "VALIDATION_ERROR"
    And match response.details contains deep [{ field: 'subjectId', reason: '#string' }]
