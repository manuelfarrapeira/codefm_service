@Regresion
Feature: Teacher Notebook - Update Student Criterion

  Background:
    * configure headers = { Cookie: '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl


  Scenario: Should fail when student criterion assignment does not exist
    * def body = { criterionId: 1 }
    Given path '/teacher-notebook/v1/student-criteria/9999'
    And request body
    When method PUT
    Then status 404

  Scenario: Should fail when student criterion assignment does not exist
    * def body = { criterionId: 1 }
    Given path '/teacher-notebook/v1/student-criteria/9999'
    And request body
    When method PUT
    Then status 404

  Scenario: Should fail when criterion does not exist
    * def body = { criterionId: 9999 }
    Given path '/teacher-notebook/v1/student-criteria/9999'
    And request body
    When method PUT
    Then status 404
