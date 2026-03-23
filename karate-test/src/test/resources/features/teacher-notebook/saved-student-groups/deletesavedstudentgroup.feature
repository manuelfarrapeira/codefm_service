@Regresion
Feature: Teacher Notebook - Delete All Saved Student Groups for a Class

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Delete all saved student groups for a class
    Given path '/teacher-notebook/v1/classes/1/saved-groups'
    When method DELETE
    Then status 204

