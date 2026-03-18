@Regresion
Feature: Teacher Notebook - Assign Criterion To Student

  Background:
    * configure headers = { Cookie: '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Should fail when class-rubric assignment does not exist
    * def body = { criterionId: 1 }
    Given path '/teacher-notebook/v1/class-rubrics/9999/students/1/criteria'
    And request body
    When method POST
    Then status 404

  Scenario: Should fail when student does not exist
    * def body = { criterionId: 1 }
    Given path '/teacher-notebook/v1/class-rubrics/1/students/9999/criteria'
    And request body
    When method POST
    Then status 400

  Scenario: Should fail when criterion does not exist
    * def body = { criterionId: 9999 }
    Given path '/teacher-notebook/v1/class-rubrics/1/students/1/criteria'
    And request body
    When method POST
    Then status 400
