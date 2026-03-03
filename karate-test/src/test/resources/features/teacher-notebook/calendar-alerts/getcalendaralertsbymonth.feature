@Regresion
Feature: Teacher Notebook - Get Calendar Alerts by Year and Month

  Background:
    * configure headers = { Cookie: '#(authTokens.karateuseradmin)' }
    Given url baseHttpsUrl

  Scenario: Get calendar alerts by year and month - Success
    * def alertSchema = { id: '#number', date: '#string', title: '#string', description: '##string', startTime: '##string', endTime: '##string' }

    Given path '/teacher-notebook/v1/calendar-alerts/2026/3'
    When method GET
    Then status 200
    And match each response == alertSchema

  Scenario: Get calendar alerts by year and month - Empty result
    Given path '/teacher-notebook/v1/calendar-alerts/2099/12'
    When method GET
    Then status 200
    And match response == '#[0]'

  Scenario: Get calendar alerts - Invalid month (0)
    Given path '/teacher-notebook/v1/calendar-alerts/2026/0'
    And header Accept-Language = 'es'
    When method GET
    Then status 400
    And match response.code == "1006"

  Scenario: Get calendar alerts - Invalid month (13)
    Given path '/teacher-notebook/v1/calendar-alerts/2026/13'
    And header Accept-Language = 'es'
    When method GET
    Then status 400
    And match response.code == "1006"

  Scenario: Get calendar alerts - Invalid year (0)
    Given path '/teacher-notebook/v1/calendar-alerts/0/3'
    And header Accept-Language = 'es'
    When method GET
    Then status 400
    And match response.code == "1006"

  Scenario: Get calendar alerts - Invalid year (-1)
    Given path '/teacher-notebook/v1/calendar-alerts/-1/3'
    And header Accept-Language = 'es'
    When method GET
    Then status 400
    And match response.code == "1006"

