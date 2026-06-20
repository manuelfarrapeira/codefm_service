@Regresion
Feature: Teacher Notebook - Create Group Assignment

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Create group assignment - Validation error (missing title)
    * def createRequest = { description: 'No title', quarter: 1 }
    Given path '/teacher-notebook/v1/classes/4/group-assignments'
    And request createRequest
    When method POST
    Then status 400
    And match response.code == "1006"
    And match response.description == "VALIDATION_ERROR"
    And match response.details contains deep [{ field: 'title', reason: '#string' }]

  Scenario: Create group assignment - Validation error (invalid quarter)
    * def createRequest = { title: 'Project', quarter: 5 }
    Given path '/teacher-notebook/v1/classes/4/group-assignments'
    And request createRequest
    When method POST
    Then status 400
    And match response.code == "1006"
    And match response.description == "VALIDATION_ERROR"
    And match response.details contains deep [{ field: 'quarter', reason: '#string' }]

  Scenario: Create group assignment - Validation error (missing quarter)
    * def createRequest = { title: 'Project' }
    Given path '/teacher-notebook/v1/classes/4/group-assignments'
    And request createRequest
    When method POST
    Then status 400
    And match response.code == "1006"
    And match response.description == "VALIDATION_ERROR"
    And match response.details contains deep [{ field: 'quarter', reason: '#string' }]

  Scenario: Create group assignment - Class not found
    * def createRequest = { title: 'Project', quarter: 1 }
    Given path '/teacher-notebook/v1/classes/9999/group-assignments'
    And request createRequest
    When method POST
    Then status 404
    And match response.code == "1003"
    And match response.description == "RESOURCE_NOT_FOUND"

