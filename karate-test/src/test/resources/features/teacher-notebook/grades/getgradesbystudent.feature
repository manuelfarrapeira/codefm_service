@Regresion
Feature: Get Grades by Class and Student

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Get grades by class and student - Success
    * def documentSchema = { id: '#number', gradeId: '#number', document: '#string', description: '##string' }
    * def exerciseGradeSchema = { gradeId: '#number', exerciseId: '#number', exerciseTitle: '#string', maxGrade: '#number', percentageGrade: '#number', grade: '#number', description: '##string', documents: '#[] documentSchema' }
    * def subjectGradeSchema = { subjectId: '#number', subjectName: '#string', exercises: '#[] exerciseGradeSchema' }
    * def quarterGradeSchema = { quarter: '#number', subjects: '#[] subjectGradeSchema' }
    Given path '/teacher-notebook/v1/classes/4/students/1/grades'
    When method GET
    Then status 200
    And match each response == quarterGradeSchema

  Scenario: Get grades by class and student - Class not found
    Given path '/teacher-notebook/v1/classes/9999/students/1/grades'
    When method GET
    Then status 404
    And match response.code == "1003"

  Scenario: Get grades by class and student - Student not in class
    Given path '/teacher-notebook/v1/classes/1/students/9999/grades'
    When method GET
    Then status 400
    And match response.code == "1006"

