@Regresion
Feature: Teacher Notebook - Delete Calendar Alert

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Fail to delete a non-existent calendar alert
    Given path '/teacher-notebook/v1/calendar-alerts/9999'
    When method DELETE
    Then status 404
    And match response.code == '1003'
    And match response.description == 'RESOURCE_NOT_FOUND'
    And match response.detail == 'Alerta de calendario no encontrada.'

