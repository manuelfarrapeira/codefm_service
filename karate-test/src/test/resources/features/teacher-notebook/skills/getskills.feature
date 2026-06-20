@Regresion
Feature: Teacher Notebook - Get Skills

  Background:
    * configure headers = { Cookie: '#(authTokens.karateuseradmin)' }
    Given url baseHttpsUrl

  Scenario: Get skills for the authenticated teacher
    * def skillSchema = { id: '#number', title: '#string', description: '#string' }

    Given path '/teacher-notebook/v1/skills'
    When method GET
    Then status 200
    And match each response == skillSchema
