@Regresion
Feature: Delete School API

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es'}
    Given url baseHttpsUrl


  Scenario: Update a school successfully
    * def requestBody =
      """
      {
        "name": "C.P Sobrada",
        "town": "Tomiño",
        "tlf": 986635445
      }
      """
    Given path '/teacher-notebook/schools/1'
    And request requestBody
    When method PATCH
    Then status 200
    And match response.id == '#number'
    And match response.name == "C.P Sobrada"
    And match response.town == "Tomiño"
    And match response.tlf == 986635445

  Scenario Outline: update school whith invalid data
    * def requestBody = { name: <name>, town: <town>, tlf: <tlf> }
    * if (requestBody.name == null) karate.remove('requestBody', 'name')
    * if (requestBody.town == null) karate.remove('requestBody', 'town')
    * if (requestBody.tlf == null) karate.remove('requestBody', 'tlf')

    Given path '/teacher-notebook/schools/' + <schoolId>
    And request requestBody
    When method PATCH
    Then status <status>
    And match response.code == <code>
    And match response.description == <description>
    And match response.details contains deep <details>

    Examples:
      | schoolId | status | code   | description        | name         | town        | tlf       | details                                                                                                                                       |
      | 1        | 400    | '1006' | 'VALIDATION_ERROR' | null         | "Some Town" | 987654321 | [{ field: 'name', reason: 'El nombre del colegio es obligatorio.' }]                                                                          |
      | 1        | 400    | '1006' | 'VALIDATION_ERROR' | "Valid Name" | "Some Town" | 123       | [{ field: 'tlf', reason: 'El número de teléfono debe tener 9 dígitos.' }]                                                                     |
      | 1        | 400    | '1006' | 'VALIDATION_ERROR' | null         | "Tomiño"    | 123       | [{ field: 'name', reason: 'El nombre del colegio es obligatorio.' }, { field: 'tlf', reason: 'El número de teléfono debe tener 9 dígitos.' }] |