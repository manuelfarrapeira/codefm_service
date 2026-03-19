@Regresion
Feature: Get Grades by Class

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Get grades by class - Success
    * def documentSchema = { id: '#number', gradeId: '#number', document: '#string', description: '##string' }
    * def isValidDocuments = function(docs) { return docs.length == 0 || karate.match(docs, '#[] documentSchema').pass }
    * def exerciseGradeSchema = { gradeId: '#number', exerciseId: '#number', exerciseTitle: '#string', maxGrade: '#number', percentageGrade: '#number', grade: '#number', description: '##string', documents: '#? isValidDocuments(_)' }
    * def subjectGradeSchema = { subjectId: '#number', subjectName: '#string', exercises: '#[] exerciseGradeSchema' }
    * def quarterGradeSchema = { quarter: '#number', subjects: '#[] subjectGradeSchema' }
    * def studentGradeSchema = { studentId: '#number', studentName: '#string', studentSurnames: '#string', quarters: '#[] quarterGradeSchema' }
    Given path '/teacher-notebook/v1/classes/4/grades'
    When method GET
    Then status 200
    And match each response == studentGradeSchema

  Scenario: Get grades by class - Class not found
    Given path '/teacher-notebook/v1/classes/9999/grades'
    When method GET
    Then status 404
    And match response.code == "1003"

