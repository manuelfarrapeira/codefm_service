@Regresion
Feature: Teacher Notebook - Get Group Assignments by Class

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Get group assignments for a class - Success
    * def assignmentSchema = { id: '#number', classId: '#number', title: '#string', description: '##string', quarter: '#number', documents: '#array' }
    Given path '/teacher-notebook/v1/classes/4/group-assignments'
    When method GET
    Then status 200
    And match each response == assignmentSchema

  Scenario: Get group assignments - Class not found
    Given path '/teacher-notebook/v1/classes/9999/group-assignments'
    When method GET
    Then status 404
    And match response.code == "1003"
    And match response.description == "RESOURCE_NOT_FOUND"

