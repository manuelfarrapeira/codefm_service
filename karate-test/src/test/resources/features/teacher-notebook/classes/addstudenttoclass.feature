@Regresion
Feature: Teacher Notebook - Add/Remove Student to Class

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Successfully remove student to class

    Given path '/teacher-notebook/v1/classes/1/students/1'
    When method DELETE
    Then status 204

 Scenario: Successfully add student to class

   Given path '/teacher-notebook/v1/classes/1/students/1'
   When method PUT
   Then status 204

  Scenario: Fail to add student that is already in the class
    Given path '/teacher-notebook/v1/classes/1/students/1'
    When method PUT
    Then status 400
    And match response.code == '1006'
    And match response.description == 'VALIDATION_ERROR'
    And match response.detail == 'El estudiante ya está matriculado en esta clase.'

  Scenario: Fail to add student when class does not exist
    Given path '/teacher-notebook/v1/classes/999/students/1'
    When method PUT
    Then status 404
    And match response.code == '1003'
    And match response.description == 'RESOURCE_NOT_FOUND'
    And match response.detail == 'Clase no encontrada.'

  Scenario: Fail to add student when student does not exist
    Given path '/teacher-notebook/v1/classes/1/students/999'
    When method PUT
    Then status 404
    And match response.code == '1003'
    And match response.description == 'RESOURCE_NOT_FOUND'
    And match response.detail == 'Estudiante no encontrado.'

  Scenario: Fail to add student from another teacher's class
    Given path '/teacher-notebook/v1/classes/100/students/1'
    When method PUT
    Then status 404
    And match response.code == '1003'
    And match response.description == 'RESOURCE_NOT_FOUND'

  Scenario: Fail to add another teacher's student to own class
    Given path '/teacher-notebook/v1/classes/1/students/100'
    When method PUT
    Then status 404
    And match response.code == '1003'
    And match response.description == 'RESOURCE_NOT_FOUND'