@Regresion
Feature: Upload Grade Document

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Upload document and then delete it - Success
    Given path '/teacher-notebook/v1/grades/6/documents'
    And multipart field description = 'Documento temporal para test'
    And multipart file file = { read: 'classpath:features/teacher-notebook/grade-documents/test-file.txt', filename: 'test-karate.pdf', contentType: 'application/pdf' }
    When method POST
    Then status 201
    And match response.id == '#number'
    And match response.gradeId == 6
    And match response.document == '#string'
    And match response.description == 'Documento temporal para test'
    * def documentId = response.id

    * def deleteFn =
      """
      function(args) {
        var HttpURLConnection = Java.type('java.net.HttpURLConnection');
        var URL = Java.type('java.net.URL');
        var url = new URL(args.baseUrl + '/teacher-notebook/v1/grades/' + args.gradeId + '/documents/' + args.documentId);
        var conn = url.openConnection();
        conn.setRequestMethod('DELETE');
        conn.setRequestProperty('Cookie', args.cookie);
        var status = conn.getResponseCode();
        conn.disconnect();
        return status;
      }
      """
    * def deleteStatus = deleteFn({ baseUrl: baseHttpsUrl, gradeId: 6, documentId: documentId, cookie: authTokens.karateuseradmin })
    * match deleteStatus == 204

  Scenario: Upload document - Validation error (no file)
    Given path '/teacher-notebook/v1/grades/6/documents'
    And multipart field description = 'Test description'
    When method POST
    Then status 500

  Scenario: Upload document - Grade not found
    Given path '/teacher-notebook/v1/grades/99999/documents'
    And multipart field description = 'Test description'
    And multipart file file = { read: 'classpath:features/teacher-notebook/grade-documents/test-file.txt', filename: 'test.pdf', contentType: 'application/pdf' }
    When method POST
    Then status 404
    And match response.code == "1003"

  Scenario: Upload document - Invalid extension
    Given path '/teacher-notebook/v1/grades/6/documents'
    And multipart field description = 'Test description'
    And multipart file file = { read: 'classpath:features/teacher-notebook/grade-documents/test-file.txt', filename: 'virus.exe', contentType: 'application/octet-stream' }
    When method POST
    Then status 500
    And match response.description == '#string'

