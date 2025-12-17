@Regresion
Feature: Teacher Notebook - Delete Class

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseLocalUrl

  Scenario Outline: Fail to delete class
    Given path '/teacher-notebook/classes/' + <classId>
    When method DELETE
    Then status <status>
    And match response.code == <code>
    And match response.description == <description>
    And match response.detail == <detail>

    Examples:
      | classId | status | code   | description          | detail                                         |
      | 999     | 404    | '1003' | 'RESOURCE_NOT_FOUND' | 'Clase no encontrada.'                         |
      | 5       | 403    | '1004' | 'RESOURCE_FORBIDDEN' | 'No está autorizado para realizar cambios en esta clase.' |


