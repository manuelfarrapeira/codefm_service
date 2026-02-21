@Regresion
Feature: Teacher Notebook - Delete Student

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

#  Scenario: Soft-delete a student successfully
#    # First create a student
#    * def createRequestBody =
#      """
#      {
#        "name": "Ana",
#        "surnames": "Sánchez Fernández",
#        "dateOfBirth": "25/11/2013",
#        "gender": "F"
#      }
#      """
#    Given path '/teacher-notebook/v1/students'
#    And request createRequestBody
#    When method PUT
#    Then status 201
#    * def studentId = response.id
#
#    # Now delete the student
#    Given path '/teacher-notebook/v1/students/' + studentId
#    When method DELETE
#    Then status 204

  Scenario: Fail to delete a non-existent student
    Given path '/teacher-notebook/v1/students/0'
    When method DELETE
    Then status 404
    And match response.code == "1003"
    And match response.description == "RESOURCE_NOT_FOUND"

