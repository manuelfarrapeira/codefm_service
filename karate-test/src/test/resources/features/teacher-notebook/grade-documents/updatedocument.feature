@Regresion
Feature: Update Grade Document Description

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Update document description - Success
    Given path '/teacher-notebook/v1/grades/6/documents/1'
    And request { description: 'Descripcion actualizada' }
    When method PATCH
    Then status 200
    And match response.description == 'Descripcion actualizada'

  Scenario: Update document description - Grade not found
    Given path '/teacher-notebook/v1/grades/99999/documents/1'
    And request { description: 'Nueva descripcion' }
    When method PATCH
    Then status 404
    And match response.code == "1003"

  Scenario: Update document description - Document not found
    Given path '/teacher-notebook/v1/grades/1/documents/99999'
    And request { description: 'Nueva descripcion' }
    When method PATCH
    Then status 404
    And match response.code == "1003"

