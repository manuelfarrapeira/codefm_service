@Regresion
Feature: Teacher Notebook - Schools

  Background:
    * configure headers = { Cookie: '#(authTokens.karateuseradmin)' }
    Given url baseHttpsUrl

  Scenario: Get schools for the authenticated teacher
    * def classSchema = { id: '#number', schoolId: '#number', name: '#string', schoolYear: '#string' }
    * def schoolSchema = { id: '#number', name: '#string', town: '#string', tlf: '#number', classes: '#[] classSchema' }

    Given path '/teacher-notebook/schools'
    When method GET
    Then status 200
    And match each response == schoolSchema
