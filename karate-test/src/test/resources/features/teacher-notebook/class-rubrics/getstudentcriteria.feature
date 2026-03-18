@Regresion
Feature: Teacher Notebook - Get Student Criteria By Student

  Background:
    * configure headers = { Cookie: '#(authTokens.karateuseradmin)' }
    Given url baseHttpsUrl

  Scenario: Get criterion assignments for a specific student in a class grouped
    * def rubricSchema = { id: '#number', title: '#string' }
    * def criterionSchema = { id: '#number', description: '#string', gradeStart: '#number', gradeEnd: '#number' }
    * def assignmentSchema = { id: '#number', classRubricId: '#number', rubric: '#(rubricSchema)', criterion: '#(criterionSchema)' }
    * def studentSchema = { id: '#number', name: '#string', surnames: '#string' }
    * def groupSchema = { student: '#(studentSchema)', rubricCriteria: '#[] assignmentSchema' }

    Given path '/teacher-notebook/v1/classes/1/students/1/rubric-criteria'
    When method GET
    Then status 200
    And match each response == groupSchema

  Scenario: Get student criteria for a non-existent class returns 404
    Given path '/teacher-notebook/v1/classes/9999/students/1/rubric-criteria'
    When method GET
    Then status 404

  Scenario: Get student criteria for a non-existent student returns 404
    Given path '/teacher-notebook/v1/classes/1/students/9999/rubric-criteria'
    When method GET
    Then status 404

