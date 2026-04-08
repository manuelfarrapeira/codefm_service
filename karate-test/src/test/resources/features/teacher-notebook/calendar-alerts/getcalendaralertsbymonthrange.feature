@Regresion
Feature: Teacher Notebook - Get Calendar Alerts by Year and Month Range

  Background:
    * configure headers = { Cookie: '#(authTokens.karateuseradmin)' }
    Given url baseHttpsUrl

  Scenario: Get calendar alerts by year and month range - Success
    * def alertSchema = { id: '#number', date: '#string', title: '#string', description: '##string', startTime: '##string', endTime: '##string' }

    Given path '/teacher-notebook/v1/calendar-alerts/2026/1/6'
    When method GET
    Then status 200
    And match each response == alertSchema

  Scenario: Get calendar alerts by year and month range - Same start and end month
    * def alertSchema = { id: '#number', date: '#string', title: '#string', description: '##string', startTime: '##string', endTime: '##string' }

    Given path '/teacher-notebook/v1/calendar-alerts/2026/3/3'
    When method GET
    Then status 200
    And match each response == alertSchema

  Scenario: Get calendar alerts by year and month range - Full year
    Given path '/teacher-notebook/v1/calendar-alerts/2026/1/12'
    When method GET
    Then status 200

  Scenario: Get calendar alerts by year and month range - Empty result
    Given path '/teacher-notebook/v1/calendar-alerts/2099/1/12'
    When method GET
    Then status 200
    And match response == '#[0]'

  Scenario: Get calendar alerts by year and month range - Invalid end month before start month
    Given path '/teacher-notebook/v1/calendar-alerts/2026/6/3'
    And header Accept-Language = 'es'
    When method GET
    Then status 400
    And match response.code == "1006"

  Scenario: Get calendar alerts by year and month range - Invalid start month (0)
    Given path '/teacher-notebook/v1/calendar-alerts/2026/0/6'
    And header Accept-Language = 'es'
    When method GET
    Then status 400
    And match response.code == "1006"

  Scenario: Get calendar alerts by year and month range - Invalid end month (13)
    Given path '/teacher-notebook/v1/calendar-alerts/2026/1/13'
    And header Accept-Language = 'es'
    When method GET
    Then status 400
    And match response.code == "1006"

  Scenario: Get calendar alerts by year and month range - Invalid year (0)
    Given path '/teacher-notebook/v1/calendar-alerts/0/1/6'
    And header Accept-Language = 'es'
    When method GET
    Then status 400
    And match response.code == "1006"

  Scenario: Get calendar alerts by year and month range - Invalid year (-1)
    Given path '/teacher-notebook/v1/calendar-alerts/-1/1/6'
    And header Accept-Language = 'es'
    When method GET
    Then status 400
    And match response.code == "1006"

