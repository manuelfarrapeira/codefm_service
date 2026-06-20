@Regresion
Feature: Download Grade Document

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Download document - Success
    Given path '/teacher-notebook/v1/grades/6/documents/9'
    When method GET
    Then status 200

  Scenario: Download document - Grade not found
    Given path '/teacher-notebook/v1/grades/99999/documents/1'
    When method GET
    Then status 404
    And match response.code == "1003"

  Scenario: Download document - Document not found
    Given path '/teacher-notebook/v1/grades/1/documents/99999'
    When method GET
    Then status 404
    And match response.code == "1003"

