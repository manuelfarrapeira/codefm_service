@Regresion
Feature: Teacher Notebook - Create Saved Student Groups

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Create saved student groups with validation error (student not enrolled)
    Given path '/teacher-notebook/v1/classes/1/saved-groups'
    And request [{ name: 'Group A', studentIds: [1, 2] }]
    When method POST
    Then status 400
    And match response ==
    """
    {
      "code" : "1006",
      "description" : "VALIDATION_ERROR",
      "detail" : null,
      "details" : [ {
        "field" : "studentIds",
        "reason" : "El alumno no está matriculado en esta clase: 2."
      } ]
    }
    """
