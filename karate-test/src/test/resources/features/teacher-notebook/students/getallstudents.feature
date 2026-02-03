Feature: Get All Students Endpoint

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl
    * def studentSchema = { id: '#number', name: '#string', surnames: '#string', dateOfBirth: '##string', additionalInfo: '##string', photo: '##string' }

  Scenario: Get all students successfully
    Given path '/teacher-notebook/v1/students/all'
    When method GET
    Then status 200
    And match response == '#[]'
    And match each response == studentSchema


