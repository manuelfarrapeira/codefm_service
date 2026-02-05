@Regresion
Feature: Teacher Notebook - Create Subject

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl


  Scenario Outline: Fail to create a subject with invalid data
    * def requestBody = { name: <name> }
    * if (requestBody.name == null) karate.remove('requestBody', 'name')

    Given path '/teacher-notebook/v1/subjects'
    And request requestBody
    When method PUT
    Then status 400
    And match response.code == "1006"
    And match response.description == "VALIDATION_ERROR"
    And match response.details contains deep <details>

    Examples:
      | name | details                                                                   |
      | null | [{ field: 'name', reason: 'El nombre de la asignatura es obligatorio.' }] |
      | ""   | [{ field: 'name', reason: 'El nombre de la asignatura es obligatorio.' }] |
      | "  " | [{ field: 'name', reason: 'El nombre de la asignatura es obligatorio.' }] |