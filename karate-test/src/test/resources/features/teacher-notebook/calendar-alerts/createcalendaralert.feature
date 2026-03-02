@Regresion
Feature: Teacher Notebook - Create Calendar Alert

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Create a calendar alert successfully and then delete it
    * def requestBody = { date: '15/03/2026', title: 'Test alert', description: 'Test description', startTime: '09:00', endTime: '10:00' }
    Given path '/teacher-notebook/v1/calendar-alerts'
    And request requestBody
    When method PUT
    Then status 201
    And match response.id == '#number'
    And match response.date == '15/03/2026'
    And match response.title == 'Test alert'
    And match response.description == 'Test description'
    And match response.startTime == '09:00'
    And match response.endTime == '10:00'
    * def createdId = response.id

    * def deleteFn =
      """
      function(args) {
        var HttpURLConnection = Java.type('java.net.HttpURLConnection');
        var URL = Java.type('java.net.URL');
        var url = new URL(args.baseUrl + '/teacher-notebook/v1/calendar-alerts/' + args.alertId);
        var conn = url.openConnection();
        conn.setRequestMethod('DELETE');
        conn.setRequestProperty('Cookie', args.cookie);
        var status = conn.getResponseCode();
        conn.disconnect();
        return status;
      }
      """
    * def deleteStatus = deleteFn({ baseUrl: baseHttpsUrl, alertId: createdId, cookie: authTokens.karateuseradmin })
    * match deleteStatus == 204

  Scenario Outline: Fail to create a calendar alert with invalid data
    * def requestBody = { date: <date>, title: <title> }
    * if (requestBody.date == null) karate.remove('requestBody', 'date')
    * if (requestBody.title == null) karate.remove('requestBody', 'title')

    Given path '/teacher-notebook/v1/calendar-alerts'
    And request requestBody
    When method PUT
    Then status 400
    And match response.code == "1006"
    And match response.description == "VALIDATION_ERROR"
    And match response.details contains deep <details>

    Examples:
      | date         | title  | details                                                                                          |
      | '15/03/2026' | null   | [{ field: 'title', reason: 'El título de la alerta es obligatorio.' }]                           |
      | '15/03/2026' | ""     | [{ field: 'title', reason: 'El título de la alerta es obligatorio.' }]                           |
      | null         | "Test" | [{ field: 'date', reason: 'La fecha de la alerta es obligatoria.' }]                             |

  Scenario: Fail to create a calendar alert with endTime but no startTime
    * def requestBody = { date: '15/03/2026', title: 'Valid title', endTime: '10:00' }
    Given path '/teacher-notebook/v1/calendar-alerts'
    And request requestBody
    When method PUT
    Then status 400
    And match response.code == "1006"
    And match response.description == "VALIDATION_ERROR"
    And match response.details contains deep [{ field: 'endTime', reason: 'La hora de fin requiere una hora de inicio.' }]

  Scenario: Fail to create a calendar alert with endTime before startTime
    * def requestBody = { date: '15/03/2026', title: 'Valid title', startTime: '10:00', endTime: '09:00' }
    Given path '/teacher-notebook/v1/calendar-alerts'
    And request requestBody
    When method PUT
    Then status 400
    And match response.code == "1006"
    And match response.description == "VALIDATION_ERROR"
    And match response.details contains deep [{ field: 'endTime', reason: 'La hora de fin debe ser posterior a la hora de inicio.' }]

