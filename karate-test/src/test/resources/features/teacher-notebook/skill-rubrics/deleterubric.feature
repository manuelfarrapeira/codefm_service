@Regresion
Feature: Teacher Notebook - Delete Skill Rubric

  Background:
    * configure headers = { Cookie: '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Delete a rubric not exists
    Given path '/teacher-notebook/v1/rubrics/999'
    When method DELETE
    Then status 404

