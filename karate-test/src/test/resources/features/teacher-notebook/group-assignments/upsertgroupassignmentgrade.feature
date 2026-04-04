@Regresion
Feature: Teacher Notebook - Upsert Group Assignment Grade

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Upsert grade - Success
    * def gradeRequest = { grade: 8.5 }
    Given path '/teacher-notebook/v1/group-assignments/1/groups/41/grade'
    And request gradeRequest
    When method PUT
    Then status 200
    And match response.groupAssignmentId == 1
    And match response.groupId == 41
    And match response.grade == 8.5

  Scenario: Upsert grade - Assignment not found
    * def gradeRequest = { grade: 8.5 }
    Given path '/teacher-notebook/v1/group-assignments/9999/groups/1/grade'
    And request gradeRequest
    When method PUT
    Then status 404
    And match response.code == "1003"
    And match response.description == "RESOURCE_NOT_FOUND"

  Scenario: Upsert grade - Validation error (grade out of range)
    * def gradeRequest = { grade: 11 }
    Given path '/teacher-notebook/v1/group-assignments/9999/groups/1/grade'
    And request gradeRequest
    When method PUT
    Then status 404

