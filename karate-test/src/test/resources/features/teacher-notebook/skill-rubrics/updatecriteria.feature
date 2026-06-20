@Regresion
Feature: Teacher Notebook - Update Skill Rubric Criterion

  Background:
    * configure headers = { Cookie: '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Update a single criterion
    * def body =
    """
    {
      "description": "Lo hace mal",
      "qualification": "Insuficiente",
      "gradeStart": 0,
      "gradeEnd": 4
    }
    """

    Given path '/teacher-notebook/v1/rubrics/1/criteria/1'
    And request body
    When method PATCH
    Then status 200

