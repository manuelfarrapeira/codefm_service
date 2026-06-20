@Regresion
Feature: Teacher Notebook - Upload Group Assignment Document

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Upload assignment document - Assignment not found
    Given path '/teacher-notebook/v1/group-assignments/9999/documents'
    And multipart file file = { read: 'classpath:features/teacher-notebook/group-assignments/test-file.txt', filename: 'testfile.pdf', contentType: 'application/pdf' }
    And multipart field description = 'Test doc'
    When method POST
    Then status 404
    And match response.code == "1003"
    And match response.description == "RESOURCE_NOT_FOUND"

