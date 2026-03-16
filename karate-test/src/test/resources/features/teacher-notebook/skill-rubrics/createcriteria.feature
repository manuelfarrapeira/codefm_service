@Regresion
Feature: Teacher Notebook - Create Skill Rubric Criterion

  Background:
    * configure headers = { Cookie: '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

#  Scenario: Create a single criterion for a rubric
#    * def body =
#    """
#    {
#      "description": "Performs poorly",
#      "gradeStart": 0,
#      "gradeEnd": 4
#    }
#    """
#
#    Given path '/teacher-notebook/v1/rubrics/1/criteria'
#    And request body
#    When method PUT
#    Then status 201
#    And match response.description == 'Performs poorly'
#    And match response.id == '#number'

  Scenario: Should fail when grade range overlaps with existing
    * def body =
    """
    {
      "description": "Overlapping",
      "gradeStart": 3,
      "gradeEnd": 6
    }
    """

    Given path '/teacher-notebook/v1/rubrics/1/criteria'
    And request body
    When method PUT
    Then status 400

