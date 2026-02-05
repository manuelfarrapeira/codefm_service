@Regresion
Feature: Teacher Notebook - Delete Subject

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario Outline: Fail to delete subject
    Given path '/teacher-notebook/v1/subjects/' + <subjectId>
    When method DELETE
    Then status <status>
    And match response.code == <code>
    And match response.description == <description>
    And match response.detail == <detail>

    Examples:
      | subjectId | status | code   | description          | detail                                                         |
      | 999       | 404    | '1003' | 'RESOURCE_NOT_FOUND' | 'Asignatura no encontrada.'                                    |
      | 4         | 403    | '1004' | 'RESOURCE_FORBIDDEN' | 'No está autorizado para realizar cambios en esta asignatura.' |
