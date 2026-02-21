@Regresion
Feature: Teacher Notebook - Get Subjects

  Background:
    * configure headers = { Cookie: '#(authTokens.karateuseradmin)' }
    Given url baseHttpsUrl

  Scenario: Get subjects for the authenticated teacher
    * def subjectSchema = { id: '#number', name: '#string' }

    Given path '/teacher-notebook/v1/subjects'
    When method GET
    Then status 200
    And match each response == subjectSchema
    