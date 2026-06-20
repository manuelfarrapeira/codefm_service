@Regresion
Feature: Teacher Notebook - Update Calendar Alert

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Update a calendar alert successfully
    * def requestBody =
      """
      {
        "date": "20/04/2026",
        "title": "Updated alert title",
        "description": "Updated description",
        "startTime": "09:00",
        "endTime": "10:30"
      }
      """
    Given path '/teacher-notebook/v1/calendar-alerts/1'
    And request requestBody
    When method PATCH
    Then status 200
    And match response.id == '#number'
    And match response.date == '20/04/2026'
    And match response.title == 'Updated alert title'
    And match response.description == 'Updated description'
    And match response.startTime == '09:00'
    And match response.endTime == '10:30'

  Scenario Outline: Update calendar alert with invalid data
    * def requestBody = { date: <date>, title: <title> }
    * if (requestBody.date == null) karate.remove('requestBody', 'date')
    * if (requestBody.title == null) karate.remove('requestBody', 'title')

    Given path '/teacher-notebook/v1/calendar-alerts/' + <alertId>
    And request requestBody
    When method PATCH
    Then status <status>
    And match response.code == <code>
    And match response.description == <description>

    Examples:
      | alertId | status | code   | description          | date         | title  |
      | 1       | 400    | '1006' | 'VALIDATION_ERROR'   | '15/03/2026' | null   |
      | 1       | 400    | '1006' | 'VALIDATION_ERROR'   | '15/03/2026' | ""     |

  Scenario: Fail to update a non-existent calendar alert
    * def requestBody = { date: '15/03/2026', title: 'Valid title' }
    Given path '/teacher-notebook/v1/calendar-alerts/9999'
    And request requestBody
    When method PATCH
    Then status 404
    And match response.code == '1003'
    And match response.description == 'RESOURCE_NOT_FOUND'

