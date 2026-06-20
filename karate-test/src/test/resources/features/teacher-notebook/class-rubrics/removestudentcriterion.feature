@Regresion
Feature: Teacher Notebook - Remove Student Criterion

  Background:
    * configure headers = { Cookie: '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Remove a student criterion assignment that does not exist returns 404
    Given path '/teacher-notebook/v1/student-criteria/9999'
    When method DELETE
    Then status 404
