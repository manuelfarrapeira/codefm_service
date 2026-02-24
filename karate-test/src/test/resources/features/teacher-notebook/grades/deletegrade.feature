@Regresion
Feature: Delete Grade

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Delete grade - Grade not found
    Given path '/teacher-notebook/v1/grades/9999'
    When method DELETE
    Then status 404
    And match response.code == "1003"

