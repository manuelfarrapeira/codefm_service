@Regresion
Feature: Export Grades by Class

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Export grades by class - Success
    Given path '/teacher-notebook/v1/classes/4/grades/export'
    When method GET
    Then status 200
    And match responseHeaders['Content-Type'][0] contains 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'

  Scenario: Export grades by class - Class not found
    Given path '/teacher-notebook/v1/classes/9999/grades/export'
    When method GET
    Then status 404
    And match response.code == "1003"

