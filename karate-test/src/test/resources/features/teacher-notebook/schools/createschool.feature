@Regresion
Feature: Teacher Notebook - Create School

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

#  Scenario: Create a new school successfully
#    * def requestBody =
#      """
#      {
#        "name": "New Karate School",
#        "town": "Karate Town",
#        "tlf": 987654321
#      }
#      """
#    Given path '/teacher-notebook/schools'
#    And request requestBody
#    When method POST
#    Then status 201
#    And match response.id == '#number'
#    And match response.name == "New Karate School"
#    And match response.town == "Karate Town"
#    And match response.tlf == 987654321

  Scenario Outline: Fail to create a school with invalid data
    * def requestBody = { name: <name>, town: <town>, tlf: <tlf> }
    * if (requestBody.name == null) karate.remove('requestBody', 'name')
    * if (requestBody.town == null) karate.remove('requestBody', 'town')
    * if (requestBody.tlf == null) karate.remove('requestBody', 'tlf')

    Given path '/teacher-notebook/schools'
    And request requestBody
    When method PUT
    Then status 400
    And match response.code == "1006"
    And match response.description == "VALIDATION_ERROR"
    And match response.details contains deep <details>

    Examples:
      | name         | town        | tlf       | details                                                                                                   |
      | null         | "Some Town" | 987654321 | [{ field: 'name', reason: 'El nombre del colegio es obligatorio.' }]                                       |
      | "Valid Name" | "Some Town" | 123       | [{ field: 'tlf', reason: 'El número de teléfono debe tener 9 dígitos.' }]                                  |
      | null         | null        | 12345     | [{ field: 'name', reason: 'El nombre del colegio es obligatorio.' }, { field: 'tlf', reason: 'El número de teléfono debe tener 9 dígitos.' }] |
