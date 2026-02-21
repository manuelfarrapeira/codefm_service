@Regresion
Feature: Teacher Notebook - Get All Classes With Subjects

  Background:
    * configure headers = { Cookie: '#(authTokens.karateuseradmin)' }
    Given url baseHttpsUrl

  Scenario: Get all classes with their subjects successfully
    * def classWithSubjectsSchema =
    """
    {
      classData: {
        id: '#number',
        schoolId: '#number',
        name: '#string',
        schoolYear: '#string'
      },
      subjects: '#array'
    }
    """

    Given path '/teacher-notebook/v1/classes-subjects'
    When method GET
    Then status 200
    And match each response == classWithSubjectsSchema

