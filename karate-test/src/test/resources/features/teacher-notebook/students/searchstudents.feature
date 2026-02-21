@Regresion
Feature: Teacher Notebook - Search Students

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Search students by name successfully
    # Search by name
    Given path '/teacher-notebook/v1/students'
    And param name = 'Antonio'
    When method GET
    Then status 200
    And match response == '#array'
    And match response[*].name contains 'Antonio'


  Scenario: Search students by surnames successfully

    # Search by surnames
    Given path '/teacher-notebook/v1/students'
    And param surnames = 'González alonso'
    When method GET
    Then status 200
    And match response == '#array'
    And match response[*].surnames contains 'González alonso'

  Scenario: Search students by id successfully

    # Search by id
    Given path '/teacher-notebook/v1/students'
    And param id = 1
    When method GET
    Then status 200
    And match response == '#array'
    And match response[0].id == 1
    And match response[0].name == 'Antonio'
    And match response[0].surnames == 'González alonso'

  Scenario: Search students with multiple criteria
    # Search by name and surnames
    Given path '/teacher-notebook/v1/students'
    And param name = 'Antonio'
    And param surnames = 'González alonso'
    When method GET
    Then status 200
    And match response == '#array'
    And match response[0].name == 'Antonio'
    And match response[0].surnames == 'González alonso'

  Scenario: Search students returns empty array when no matches found
    Given path '/teacher-notebook/v1/students'
    And param name = 'NoExisteEsteNombre12345'
    When method GET
    Then status 200
    And match response == '#array'
    And match response == []

  Scenario: Fail to search students without any criteria
    Given path '/teacher-notebook/v1/students'
    When method GET
    Then status 400
    And match response.code == "1006"
    And match response.description == "VALIDATION_ERROR"
    And match response.detail contains deep "Debe proporcionar al menos un filtro (id, nombre o apellidos)."


