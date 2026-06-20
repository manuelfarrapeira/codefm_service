@Regresion
Feature: Teacher Notebook - Update Skill Rubric

  Background:
    * configure headers = { Cookie: '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Update rubric title
    * def body =
    """
    {
      "title": "Rúbrica de pensamiento crítico"
    }
    """

    Given path '/teacher-notebook/v1/rubrics/1'
    And request body
    When method PATCH
    Then status 200
    And match response.title == 'Rúbrica de pensamiento crítico'


    Scenario: Update rubric does not exist
      * def body =
      """
      {
        "title": "Rúbrica de pensamiento crítico"
      }
      """

      Given path '/teacher-notebook/v1/rubrics/999'
      And request body
      When method PATCH
      Then status 404

