@Regresion
Feature: Teacher Notebook - Update Subject

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Update a subject successfully
    * def requestBody =
      """
      {
        "name": "Matemáticas"
      }
      """
    Given path '/teacher-notebook/v1/subjects/1'
    And request requestBody
    When method PATCH
    Then status 200
    And match response.id == '#number'
    And match response.name == "Matemáticas"

  Scenario Outline: Update subject with invalid data
    * def requestBody = { name: <name> }
    * if (requestBody.name == null) karate.remove('requestBody', 'name')

    Given path '/teacher-notebook/v1/subjects/' + <subjectId>
    And request requestBody
    When method PATCH
    Then status <status>
    And match response.code == <code>
    And match response.description == <description>
    And match response.details contains deep <details>

    Examples:
      | subjectId | status | code   | description        | name | details                                                                   |
      | 1         | 400    | '1006' | 'VALIDATION_ERROR' | null | [{ field: 'name', reason: 'El nombre de la asignatura es obligatorio.' }] |
      | 1         | 400    | '1006' | 'VALIDATION_ERROR' | ""   | [{ field: 'name', reason: 'El nombre de la asignatura es obligatorio.' }] |

  Scenario Outline: Fail to update subject with access errors
    * def requestBody = { name: 'New Name' }

    Given path '/teacher-notebook/v1/subjects/' + <subjectId>
    And request requestBody
    When method PATCH
    Then status <status>
    And match response.code == <code>
    And match response.description == <description>
    And match response.detail == <detail>

    Examples:
      | subjectId | status | code   | description          | detail                                                         |
      | 999       | 404    | '1003' | 'RESOURCE_NOT_FOUND' | 'Asignatura no encontrada.'                                    |
      | 4         | 403    | '1004' | 'RESOURCE_FORBIDDEN' | 'No está autorizado para realizar cambios en esta asignatura.' |
