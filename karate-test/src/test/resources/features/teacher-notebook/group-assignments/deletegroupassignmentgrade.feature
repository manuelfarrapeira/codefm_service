@Regresion
Feature: Teacher Notebook - Delete Group Assignment Grade

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Delete grade - Assignment not found
    Given path '/teacher-notebook/v1/group-assignments/9999/groups/1/grade'
    When method DELETE
    Then status 404
    And match response.code == "1003"
    And match response.description == "RESOURCE_NOT_FOUND"

