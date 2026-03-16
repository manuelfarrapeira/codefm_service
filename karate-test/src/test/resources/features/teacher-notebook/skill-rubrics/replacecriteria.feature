@Regresion
Feature: Teacher Notebook - Create Skill Rubric Criteria

  Background:
    * configure headers = { Cookie: '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Create criteria for a rubric with valid data
    * def body =
    """
    [
      { "description": "Performs poorly", "gradeStart": 0, "gradeEnd": 4 },
      { "description": "Performs averagely", "gradeStart": 5, "gradeEnd": 6 },
      { "description": "Performs excellently", "gradeStart": 7, "gradeEnd": 10 }
    ]
    """

    Given path '/teacher-notebook/v1/rubrics/1/criteria'
    And request body
    When method PUT
    Then status 201
    And match response == '#[3]'

  Scenario: Should fail when less than 3 criteria
    * def body =
    """
    [
      { "description": "A", "gradeStart": 0, "gradeEnd": 5 },
      { "description": "B", "gradeStart": 6, "gradeEnd": 10 }
    ]
    """

    Given path '/teacher-notebook/v1/rubrics/1/criteria'
    And request body
    When method PUT
    Then status 400

