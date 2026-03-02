@Regresion
Feature: Update Grade

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Update grade - Success
    * def updateRequest = { grade: 8, description: 'Updated description' }
    Given path '/teacher-notebook/v1/grades/6'
    And request updateRequest
    When method PATCH
    Then status 200
    And match response.id == 6
    And match response.grade == 8
    And match response.description == 'Updated description'

  Scenario: Update grade - Grade not found
    * def updateRequest = { grade: 9, description: 'Updated' }
    Given path '/teacher-notebook/v1/grades/9999'
    And request updateRequest
    When method PATCH
    Then status 404
    And match response.code == "1003"

  Scenario: Update grade - Validation error (grade exceeds max)
    * def updateRequest = { grade: 999, description: 'Too high' }
    Given path '/teacher-notebook/v1/grades/6'
    And request updateRequest
    When method PATCH
    Then status 400
    And match response.code == "1006"
    And match response.details contains deep [{ field: 'grade', reason: '#string' }]
