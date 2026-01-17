@Regresion
Feature: Teacher Notebook - Upload Student Photo

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Fail to upload a photo for a non-existent student
    Given path '/teacher-notebook/v1/students/99999/photo'
    And multipart file file = { read: 'classpath:test-photo.jpg', contentType: 'image/jpeg' }
    When method POST
    Then status 404
    And match response.code == "1003"
    And match response.description == "RESOURCE_NOT_FOUND"

 Scenario: Upload a photo for a student successfully
   Given path '/teacher-notebook/v1/students/1/photo'
   And multipart file file = { read: 'classpath:test-photo.jpg', contentType: 'image/jpeg' }
   When method POST
   Then status 200

