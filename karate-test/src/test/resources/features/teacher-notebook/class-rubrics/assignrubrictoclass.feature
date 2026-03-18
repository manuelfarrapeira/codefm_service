@Regresion
Feature: Teacher Notebook - Assign Rubric To Class

  Background:
    * configure headers = { Cookie: '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Should fail when rubric does not exist
    * def body = { rubricId: 9999 }
    Given path '/teacher-notebook/v1/classes/1/rubrics'
    And request body
    When method POST
    Then status 400

  Scenario: Should fail when class does not exist
    * def body = { rubricId: 1 }
    Given path '/teacher-notebook/v1/classes/9999/rubrics'
    And request body
    When method POST
    Then status 400

