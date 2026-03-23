@Regresion
Feature: Teacher Notebook - Get Saved Student Groups

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Get saved student groups for a class
    * def memberSchema = { id: '#number', studentId: '#number', studentName: '#string', studentSurnames: '#string' }
    * def groupSchema = { id: '#number', classId: '#number', name: '#string', members: '#[] memberSchema' }
    Given path '/teacher-notebook/v1/classes/4/saved-groups'
    When method GET
    Then status 200
    And match each response == groupSchema
