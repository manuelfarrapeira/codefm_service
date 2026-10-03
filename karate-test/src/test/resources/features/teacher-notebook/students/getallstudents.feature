@Regresion
Feature: Get All Students Endpoint

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl
    * def studentSchema = { id: '#number', name: '#string', surnames: '#string', dateOfBirth: '##string', gender: '##string', additionalInfo: '##string', photo: '##string', shape: '##string', classNumber: '##number', classIds: '#[]', classList: '#[]' }
    * def schoolSchema = { schoolName: '#string', classes: '#[]' }
    * def classSchema = { className: '#string' }

  Scenario: Get all students successfully
    Given path '/teacher-notebook/v1/students/all'
    When method GET
    Then status 200
    And match response == '#[]'
    And match each response == studentSchema
    And match each response[*].classList == schoolSchema
    And match each response[*].classList[*].classes == classSchema

