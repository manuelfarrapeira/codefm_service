@Regresion
Feature: Teacher Notebook - Get Skill Rubric Criteria

  Background:
    * configure headers = { Cookie: '#(authTokens.karateuseradmin)' }
    Given url baseHttpsUrl

  Scenario: Get criteria for a rubric
    * def criteriaSchema = { id: '#number', description: '#string', qualification: '##string', gradeStart: '#number', gradeEnd: '#number' }

    Given path '/teacher-notebook/v1/rubrics/1/criteria'
    When method GET
    Then status 200
    And match each response == criteriaSchema

