@Regresion
Feature: Teacher Notebook - Download Student Photo

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Fail to download a photo for a non-existent student
    Given path '/teacher-notebook/v1/students/99999/photo'
    When method GET
    Then status 404
    And match response.code == "1003"
    And match response.description == "RESOURCE_NOT_FOUND"

  Scenario: Fail to download a photo when student has no photo
    Given path '/teacher-notebook/v1/students/8/photo'
    When method GET
    Then status 404
    And match response.code == "1003"
    And match response.description == "RESOURCE_NOT_FOUND"

  Scenario: Download a student photo successfully
    Given path '/teacher-notebook/v1/students/1/photo'
    When method GET
    Then status 200
    And match header Content-Type == 'image/jpeg'
    And match header Content-Length != null

