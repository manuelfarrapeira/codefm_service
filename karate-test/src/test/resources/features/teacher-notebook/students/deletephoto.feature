@Regresion
Feature: Teacher Notebook - Delete Student Photo

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Fail to delete a photo for a non-existent student
    Given path '/teacher-notebook/v1/students/99999/photo'
    When method DELETE
    Then status 404
    And match response.code == "1003"
    And match response.description == "RESOURCE_NOT_FOUND"

  Scenario: Fail to delete a photo when student has no photo
    Given path '/teacher-notebook/v1/students/8/photo'
    When method DELETE
    Then status 404
    And match response.code == "1003"
    And match response.description == "RESOURCE_NOT_FOUND"


