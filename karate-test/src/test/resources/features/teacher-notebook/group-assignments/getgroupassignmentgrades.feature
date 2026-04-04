@Regresion
Feature: Teacher Notebook - Get Group Assignment Grades

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Get group assignment grades - Success
    * def gradeSchema = { id: '#number', groupAssignmentId: '#number', groupId: '#number', grade: '#number', groupName: '##string', documents: '#array' }
    Given path '/teacher-notebook/v1/group-assignments/1/grades'
    When method GET
    Then status 200
    And match each response == gradeSchema

  Scenario: Get group assignment grades - Assignment not found
    Given path '/teacher-notebook/v1/group-assignments/9999/grades'
    When method GET
    Then status 404
    And match response.code == "1003"
    And match response.description == "RESOURCE_NOT_FOUND"

