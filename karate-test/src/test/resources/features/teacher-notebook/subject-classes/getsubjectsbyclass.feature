@Regresion
Feature: Teacher Notebook - Get Subjects by Class

  Background:
    * configure headers = { Cookie: '#(authTokens.karateuseradmin)' }
    Given url baseHttpsUrl

  Scenario: Get subjects for a class successfully
    * def subjectSchema = { id: '#number', name: '#string' }

    Given path '/teacher-notebook/v1/classes/1/subjects'
    When method GET
    Then status 200
    And match each response == subjectSchema

  Scenario: Fail to get subjects for a class that does not belong to teacher
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }

    Given path '/teacher-notebook/v1/classes/9999/subjects'
    When method GET
    Then status 403
    And match response.code == "1004"

