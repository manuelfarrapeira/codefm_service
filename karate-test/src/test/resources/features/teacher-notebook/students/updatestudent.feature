@Regresion
Feature: Teacher Notebook - Update Student

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Update an existing student successfully

    * def updateRequestBody =
      """
      {
        "name": "Usuario2",
        "surnames": "Prueba2",
        "dateOfBirth": "20/05/2012",
        "additionalInfo": "Aditional info"
      }
      """
    Given path '/teacher-notebook/v1/students/' + 8
    And request updateRequestBody
    When method PATCH
    Then status 200
    And match response.id == 8
    And match response.name == "Usuario2"
    And match response.surnames == "Prueba2"
    And match response.dateOfBirth == "20/05/2012"
    And match response.additionalInfo == "Aditional info"

  Scenario: Fail to update a non-existent student
    * def updateRequestBody =
      """
      {
        "name": "Juan",
        "surnames": "García López",
        "dateOfBirth": "15/03/2010"
      }
      """
    Given path '/teacher-notebook/v1/students/99999'
    And request updateRequestBody
    When method PATCH
    Then status 404
    And match response.code == "1003"
    And match response.description == "RESOURCE_NOT_FOUND"

  Scenario Outline: Fail to update a student with invalid data

    # Try to update with invalid data
    * def updateRequestBody = { name: <name>, surnames: <surnames>, dateOfBirth: <dateOfBirth> }
    * if (updateRequestBody.name == null) karate.remove('updateRequestBody', 'name')
    * if (updateRequestBody.surnames == null) karate.remove('updateRequestBody', 'surnames')
    * if (updateRequestBody.dateOfBirth == null) karate.remove('updateRequestBody', 'dateOfBirth')

    Given path '/teacher-notebook/v1/students/' + 8
    And request updateRequestBody
    When method PATCH
    Then status 400
    And match response.code == "1006"
    And match response.description == "VALIDATION_ERROR"
    And match response.details contains deep <details>

    Examples:
      | name     | surnames        | dateOfBirth  | details                                                                                     |
      | null     | "García López"  | "15/03/2010" | [{ field: 'name', reason: 'El nombre del estudiante es obligatorio.' }]                     |
      | "A"      | "García López"  | "15/03/2010" | [{ field: 'name', reason: 'El nombre del estudiante debe tener al menos 3 caracteres.' }]   |
      | "Juan"   | null            | "15/03/2010" | [{ field: 'surnames', reason: 'Los apellidos del estudiante son obligatorios.' }]           |
      | "Juan"   | "AB"            | "15/03/2010" | [{ field: 'surnames', reason: 'Los apellidos del estudiante deben tener al menos 3 caracteres.' }] |

