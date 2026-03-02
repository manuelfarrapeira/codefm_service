@Regresion
Feature: Teacher Notebook - Get Calendar Alerts

  Background:
    * configure headers = { Cookie: '#(authTokens.karateuseradmin)' }
    Given url baseHttpsUrl

  Scenario: Get calendar alerts for the authenticated teacher
    * def alertSchema = { id: '#number', date: '#string', title: '#string', description: '##string', startTime: '##string', endTime: '##string' }

    Given path '/teacher-notebook/v1/calendar-alerts'
    When method GET
    Then status 200
    And match each response == alertSchema

