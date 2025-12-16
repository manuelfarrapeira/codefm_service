@Regresion
Feature: Teacher Notebook - Classes

  Background:
    * configure headers = { Cookie: '#(authTokens.karateuseradmin)' }
    Given url baseHttpsUrl

  Scenario: Get classes for a specific school
    * def classSchema = { id: '#number', schoolId: '#number', name: '#string', schoolYear: '#string' }

    Given path '/teacher-notebook/classes/1'
    When method GET
    Then status 200
    And match each response == classSchema