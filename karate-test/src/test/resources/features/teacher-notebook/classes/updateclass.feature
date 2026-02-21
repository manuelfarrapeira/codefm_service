@Regresion
Feature: Teacher Notebook - Update Class

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  #  Scenario: Update a class successfully
  #    * def requestBody =
  #      """
  #      {
  #        "name": "Updated Class Name",
  #        "schoolYear": "24/25"
  #      }
  #      """
  #    Given path '/teacher-notebook/classes/1'
  #    And request requestBody
  #    When method PATCH
  #    Then status 200
  #    And match response.id == '#number'
  #    And match response.schoolId == '#number'
  #    And match response.name == "Updated Class Name"
  #    And match response.schoolYear == "24/25"

  Scenario Outline: Update class with invalid data
    * def requestBody = { name: <name>, schoolYear: <schoolYear> }
    * if (requestBody.name == null) karate.remove('requestBody', 'name')
    * if (requestBody.schoolYear == null) karate.remove('requestBody', 'schoolYear')

    Given path '/teacher-notebook/v1/classes/' + <classId>
    And request requestBody
    When method PATCH
    Then status <status>
    And match response.code == <code>
    And match response.description == <description>
    And match response.details contains deep <details>

    Examples:
      | classId | status | code   | description        | name         | schoolYear  | details                                                                                                                                                                  |
      | 1       | 400    | '1006' | 'VALIDATION_ERROR' | null         | "24/25"     | [{ field: 'name', reason: 'El nombre de la clase es obligatorio.' }]                                                                                                     |
      | 1       | 400    | '1006' | 'VALIDATION_ERROR' | "Valid Name" | null        | [{ field: 'schoolYear', reason: 'El año escolar es obligatorio.' }]                                                                                                      |
      | 1       | 400    | '1006' | 'VALIDATION_ERROR' | "Valid Name" | "invalid"   | [{ field: 'schoolYear', reason: 'El año escolar debe tener el formato NN/NN (ej: 24/25).' }]                                                                             |
      | 1       | 400    | '1006' | 'VALIDATION_ERROR' | "Valid Name" | "23/25"     | [{ field: 'schoolYear', reason: 'Los números del año escolar deben ser consecutivos (ej: 24/25).' }]                                                                     |
      | 1       | 400    | '1006' | 'VALIDATION_ERROR' | null         | "23/25"     | [{ field: 'name', reason: 'El nombre de la clase es obligatorio.' }, { field: 'schoolYear', reason: 'Los números del año escolar deben ser consecutivos (ej: 24/25).' }] |
      | 1       | 400    | '1006' | 'VALIDATION_ERROR' | "Valid Name" | "2024/2025" | [{ field: 'schoolYear', reason: 'El año escolar debe tener el formato NN/NN (ej: 24/25).' }]                                                                             |

  Scenario Outline: Fail to update class with access errors
    * def requestBody = { name: 'New Name', schoolYear: '24/25' }

    Given path '/teacher-notebook/v1/classes/' + <classId>
    And request requestBody
    When method PATCH
    Then status <status>
    And match response.code == <code>
    And match response.description == <description>
    And match response.detail == <detail>

    Examples:
      | classId | status | code   | description          | detail                                                    |
      | 999     | 404    | '1003' | 'RESOURCE_NOT_FOUND' | 'Clase no encontrada.'                                    |
      | 5       | 403    | '1004' | 'RESOURCE_FORBIDDEN' | 'No está autorizado para realizar cambios en esta clase.' |

