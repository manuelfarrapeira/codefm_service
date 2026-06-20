@Regresion
Feature: Update Exercise Document Description

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Update document description - Success
    * def updateRequest = { description: 'Descripción actualizada' }
    Given path '/teacher-notebook/v1/exercises/1/documents/2'
    And request updateRequest
    When method PATCH
    Then status 200
    And match response.id == 2
    And match response.exerciseId == 1
    And match response.document == '#string'
    And match response.description == 'Descripción actualizada'

  Scenario: Update document description - Document not found
    * def updateRequest = { description: 'Updated description' }
    Given path '/teacher-notebook/v1/exercises/1/documents/99999'
    And request updateRequest
    When method PATCH
    Then status 404
    And match response.code == "1003"

  Scenario: Update document description - Exercise not found
    * def updateRequest = { description: 'Updated description' }
    Given path '/teacher-notebook/v1/exercises/99999/documents/1'
    And request updateRequest
    When method PATCH
    Then status 404
    And match response.code == "1003"

