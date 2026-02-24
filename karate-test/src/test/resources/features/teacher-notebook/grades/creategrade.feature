@Regresion
Feature: Create Grade

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Create grade - Validation error (missing grade)
    * def createRequest = { studentId: 1, description: 'Test' }
    Given path '/teacher-notebook/v1/exercises/1/grades'
    And request createRequest
    When method PUT
    Then status 400
    And match response.code == "1006"
    And match response.description == "VALIDATION_ERROR"
    And match response.details contains deep [{ field: 'grade', reason: '#string' }]

  Scenario: Create grade - Validation error (grade exceeds max)
    * def createRequest = { studentId: 1, grade: 999, description: 'Test' }
    Given path '/teacher-notebook/v1/exercises/1/grades'
    And request createRequest
    When method PUT
    Then status 400
    And match response.code == "1006"
    And match response.description == "VALIDATION_ERROR"
    And match response.details contains deep [{ field: 'grade', reason: '#string' }]

  Scenario: Create grade - Exercise not found
    * def createRequest = { studentId: 1, grade: 5, description: 'Test' }
    Given path '/teacher-notebook/v1/exercises/9999/grades'
    And request createRequest
    When method PUT
    Then status 404

