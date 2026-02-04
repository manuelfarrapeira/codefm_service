@Regresion
Feature: Teacher Notebook - Delete Subject

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Delete a subject successfully
    # First create a subject to delete
    * def createRequest = { "name": "Subject To Delete" }
    Given path '/teacher-notebook/v1/subjects'
    And request createRequest
    When method PUT
    Then status 201
    * def createdSubjectId = response.id

    # Now delete the created subject
    Given path '/teacher-notebook/v1/subjects/' + createdSubjectId
    When method DELETE
    Then status 204

    # Verify it's no longer in the list
    Given path '/teacher-notebook/v1/subjects'
    When method GET
    Then status 200
    And match response[*].id !contains createdSubjectId

  Scenario Outline: Fail to delete subject
    Given path '/teacher-notebook/v1/subjects/' + <subjectId>
    When method DELETE
    Then status <status>
    And match response.code == <code>
    And match response.description == <description>
    And match response.detail == <detail>

    Examples:
      | subjectId | status | code   | description          | detail                                                         |
      | 999       | 404    | '1003' | 'RESOURCE_NOT_FOUND' | 'Asignatura no encontrada.'                                    |
      | 100       | 403    | '1004' | 'RESOURCE_FORBIDDEN' | 'No está autorizado para realizar cambios en esta asignatura.' |

  Scenario: Delete subject with English language preference
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'en' }

    Given path '/teacher-notebook/v1/subjects/999'
    When method DELETE
    Then status 404
    And match response.code == '1003'
    And match response.description == 'RESOURCE_NOT_FOUND'
    And match response.detail == 'Subject not found.'
