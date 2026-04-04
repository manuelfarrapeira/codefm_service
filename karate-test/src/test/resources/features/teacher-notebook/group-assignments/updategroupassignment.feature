@Regresion
Feature: Teacher Notebook - Update Group Assignment

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Update group assignment - Not found
    * def updateRequest = { title: 'Updated', quarter: 2 }
    Given path '/teacher-notebook/v1/group-assignments/9999'
    And request updateRequest
    When method PATCH
    Then status 404
    And match response.code == "1003"
    And match response.description == "RESOURCE_NOT_FOUND"

  Scenario: Update group assignment - Validation error (empty title)
    * def updateRequest = { title: '', quarter: 1 }
    Given path '/teacher-notebook/v1/group-assignments/9999'
    And request updateRequest
    When method PATCH
    Then status 404

