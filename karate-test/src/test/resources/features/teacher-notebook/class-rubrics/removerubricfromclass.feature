@Regresion
Feature: Teacher Notebook - Remove Rubric From Class

  Background:
    * configure headers = { Cookie: '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Remove a class-rubric assignment that does not exist returns 404
    Given path '/teacher-notebook/v1/class-rubrics/9999'
    When method DELETE
    Then status 404
