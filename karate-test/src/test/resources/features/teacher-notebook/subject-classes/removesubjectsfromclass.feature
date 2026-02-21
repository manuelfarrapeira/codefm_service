@Regresion
Feature: Teacher Notebook - Remove Subjects from Class

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Fail to remove subjects with empty subjectIds
    * def requestBody = { subjectIds: [] }

    Given path '/teacher-notebook/v1/classes/1/subjects'
    And request requestBody
    When method DELETE
    Then status 400
    And match response.code == "1006"
    And match response.description == "VALIDATION_ERROR"

  Scenario: Fail to remove subjects from a class that does not belong to teacher
    * def requestBody = { subjectIds: [1, 2] }

    Given path '/teacher-notebook/v1/classes/9999/subjects'
    And request requestBody
    When method DELETE
    Then status 403

