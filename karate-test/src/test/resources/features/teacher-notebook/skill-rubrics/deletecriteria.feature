@Regresion
Feature: Teacher Notebook - Delete Skill Rubric Criterion

  Background:
    * configure headers = { Cookie: '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Soft-delete a single criterion not found
    Given path '/teacher-notebook/v1/rubrics/1/criteria/999'
    When method DELETE
    Then status 404

