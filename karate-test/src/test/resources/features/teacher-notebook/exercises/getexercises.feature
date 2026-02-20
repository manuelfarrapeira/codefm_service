@Regresion
Feature: Get Exercises by Class

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Get exercises by class - Success
    * def exerciseSchema = { id: '#number', subjectClassId: '#number', title: '#string', description: '##string', percentageGrade: '#number', maxGrade: '#number' }
    * def subjectSchema = { subjectId: '#number', subjectName: '#string', exercises: '#[] exerciseSchema' }
    * def quarterSchema = { quarter: '#number', subjects: '#[] subjectSchema' }
    Given path '/teacher-notebook/v1/classes/1/exercises'
    When method GET
    Then status 200
    And match each response == quarterSchema

  Scenario: Get exercises by class - Class not found
    Given path '/teacher-notebook/v1/classes/9999/exercises'
    When method GET
    Then status 404
    And match response.code == "1003"
    And match response.description == "RESOURCE_NOT_FOUND"

