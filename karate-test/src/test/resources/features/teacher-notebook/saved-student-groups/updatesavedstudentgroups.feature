@Regresion
Feature: Teacher Notebook - Update All Saved Student Groups

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Update saved student groups with validation errors
    Given path '/teacher-notebook/v1/classes/4/saved-groups'
    And request [{ id: 1, name: 'Group X', studentIds: [1, 3] }, { name: 'New Group', studentIds: [2, 4] }]
    When method PATCH
    Then status 400
    And match response ==
    """
    {
      "code" : "1006",
      "description" : "VALIDATION_ERROR",
      "detail" : null,
      "details" : [ {
        "field" : "id",
        "reason" : "El grupo con id 1 no existe o está dado de baja."
      }, {
        "field" : "id",
        "reason" : "Todos los grupos deben incluir su id para poder ser actualizados."
      } ]
    }
    """

