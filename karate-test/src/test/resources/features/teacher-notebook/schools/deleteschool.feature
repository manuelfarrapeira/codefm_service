@Regresion
Feature: Delete School API

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl


  Scenario Outline: Fail to delete school
    Given path '/teacher-notebook/v1/schools/' + <schoolId>
    When method DELETE
    Then status <status>
    And match response.code == <code>
    And match response.description == <description>
    And match response.detail == <detail>

    Examples:
      | schoolId | status | code   | description          | detail                                                      |
      | 999      | 404    | '1003' | 'RESOURCE_NOT_FOUND' | 'Colegio no encontrado.'                                    |
      | 13       | 403    | '1004' | 'RESOURCE_FORBIDDEN' | 'No está autorizado para realizar cambios en este colegio.' |

