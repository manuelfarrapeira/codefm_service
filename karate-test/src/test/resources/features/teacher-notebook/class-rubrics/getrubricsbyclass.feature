@Regresion
Feature: Teacher Notebook - Get Rubrics By Class

  Background:
    * configure headers = { Cookie: '#(authTokens.karateuseradmin)' }
    Given url baseHttpsUrl

  Scenario: Get rubrics for a class
    * def criteriaSchema = { id: '#number', description: '#string', gradeStart: '#number', gradeEnd: '#number' }
    * def rubricSchema = { id: '#number', classId: '#number', rubricId: '#number', rubricTitle: '#string', skillId: '#number', criteria: '#[] criteriaSchema' }

    Given path '/teacher-notebook/v1/classes/1/rubrics'
    When method GET
    Then status 200
    And match each response == rubricSchema

  Scenario: Get rubrics for a non-existent class returns 404
    Given path '/teacher-notebook/v1/classes/9999/rubrics'
    When method GET
    Then status 404

