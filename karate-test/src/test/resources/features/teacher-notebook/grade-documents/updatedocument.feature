@Regresion
Feature: Update Grade Document Description

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Update document description - Success
    Given path '/teacher-notebook/v1/grades/1/documents'
    And multipart field description = 'Descripcion original'
    And multipart file file = { read: 'classpath:features/teacher-notebook/grade-documents/test-file.txt', filename: 'test-update.pdf', contentType: 'application/pdf' }
    When method POST
    Then status 201
    * def documentId = response.id

    Given path '/teacher-notebook/v1/grades/1/documents/' + documentId
    And request { description: 'Descripcion actualizada' }
    When method PATCH
    Then status 200
    And match response.id == documentId
    And match response.description == 'Descripcion actualizada'

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
    * def deleteStatus = deleteFn({ baseUrl: baseHttpsUrl, gradeId: 1, documentId: documentId, cookie: authTokens.karateuseradmin })
    * match deleteStatus == 204

  Scenario: Update document description - Grade not found
    Given path '/teacher-notebook/v1/grades/99999/documents/1'
    And request { description: 'Nueva descripcion' }
    When method PATCH
    Then status 404
    And match response.code == "1003"

  Scenario: Update document description - Document not found
    Given path '/teacher-notebook/v1/grades/1/documents/99999'
    And request { description: 'Nueva descripcion' }
    When method PATCH
    Then status 404
    And match response.code == "1003"

