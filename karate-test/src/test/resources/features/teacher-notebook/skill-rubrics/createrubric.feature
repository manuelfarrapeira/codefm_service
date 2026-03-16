@Regresion
Feature: Teacher Notebook - Create Skill Rubric

  Background:
    * configure headers = { Cookie: '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

#  Scenario: Create a rubric with title only
#    * def body =
#    """
#    {
#      "title": "Rubric for testing"
#    }
#    """
#
#    Given path '/teacher-notebook/v1/skills/1/rubrics'
#    And request body
#    When method PUT
#    Then status 201
#    And match response.title == 'Rubric for testing'
#    And match response.id == '#number'

  Scenario: Should fail when title is empty
    * def body =
    """
    {
      "title": ""
    }
    """

    Given path '/teacher-notebook/v1/skills/1/rubrics'
    And request body
    When method PUT
    Then status 400

