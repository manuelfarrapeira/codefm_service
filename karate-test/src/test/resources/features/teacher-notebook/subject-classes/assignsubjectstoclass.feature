@Regresion
Feature: Teacher Notebook - Assign Subjects to Class

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Fail to assign subjects with empty subjectIds
    * def requestBody = { subjectIds: [] }

    Given path '/teacher-notebook/v1/classes/1/subjects'
    And request requestBody
    When method PUT
    Then status 400
    And match response.code == "1006"
    And match response.description == "VALIDATION_ERROR"

  Scenario: Fail to assign subjects to a class that does not belong to teacher
    * def requestBody = { subjectIds: [1, 2] }

    Given path '/teacher-notebook/v1/classes/9999/subjects'
    And request requestBody
    When method PUT
    Then status 403

  Scenario: Fail to assign subjects that do not belong to teacher
    * def requestBody = { subjectIds: [9999] }

    Given path '/teacher-notebook/v1/classes/1/subjects'
    And request requestBody
    When method PUT
    Then status 400
    And match response.code == "1006"

