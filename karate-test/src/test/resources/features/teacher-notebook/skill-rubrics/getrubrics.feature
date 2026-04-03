@Regresion
Feature: Teacher Notebook - Get Skill Rubrics

  Background:
    * configure headers = { Cookie: '#(authTokens.karateuseradmin)' }
    Given url baseHttpsUrl

  Scenario: Get rubrics for a skill
    * def criteriaSchema = { id: '#number', description: '#string', qualification: '##string', gradeStart: '#number', gradeEnd: '#number' }
    * def rubricSchema = { id: '#number', title: '#string', skillId: '#number', criteria: '#[] criteriaSchema' }

    Given path '/teacher-notebook/v1/skills/1/rubrics'
    When method GET
    Then status 200
    And match each response == rubricSchema

