@Regresion
Feature: Create Exercise

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

#  Scenario: Create exercise - Success
#    * def createRequest = { title: 'Karate Test Exam', description: 'Test description', quarter: 1, percentageGrade: 30, maxGrade: 10 }
#    Given path '/teacher-notebook/v1/subject-classes/1/exercises'
#    And request createRequest
#    When method PUT
#    Then status 201
#    And match response.title == 'Karate Test Exam'
#    And match response.subjectClassId == 1
#    And match response.percentageGrade == 30
#    And match response.maxGrade == 10

  Scenario: Create exercise - Validation error (missing title)
    * def createRequest = { description: 'No title', quarter: 1, percentageGrade: 30, maxGrade: 10 }
    Given path '/teacher-notebook/v1/subject-classes/1/exercises'
    And request createRequest
    When method PUT
    Then status 400
    And match response.code == "1006"
    And match response.description == "VALIDATION_ERROR"
    And match response.details contains deep [{ field: 'title', reason: '#string' }]

  Scenario: Create exercise - Validation error (invalid quarter)
    * def createRequest = { title: 'Exam', quarter: 5, percentageGrade: 30, maxGrade: 10 }
    Given path '/teacher-notebook/v1/subject-classes/1/exercises'
    And request createRequest
    When method PUT
    Then status 400
    And match response.code == "1006"
    And match response.description == "VALIDATION_ERROR"
    And match response.details contains deep [{ field: 'quarter', reason: '#string' }]

  Scenario: Create exercise - Validation error (missing quarter)
    * def createRequest = { title: 'Exam', percentageGrade: 30, maxGrade: 10 }
    Given path '/teacher-notebook/v1/subject-classes/1/exercises'
    And request createRequest
    When method PUT
    Then status 400
    And match response.code == "1006"
    And match response.description == "VALIDATION_ERROR"
    And match response.details contains deep [{ field: 'quarter', reason: '#string' }]

  Scenario: Create exercise - Validation error (missing percentageGrade)
    * def createRequest = { title: 'Exam', quarter: 1, maxGrade: 10 }
    Given path '/teacher-notebook/v1/subject-classes/1/exercises'
    And request createRequest
    When method PUT
    Then status 400
    And match response.code == "1006"
    And match response.description == "VALIDATION_ERROR"
    And match response.details contains deep [{ field: 'percentageGrade', reason: '#string' }]

  Scenario: Create exercise - Validation error (invalid percentageGrade)
    * def createRequest = { title: 'Exam', quarter: 1, percentageGrade: 101, maxGrade: 10 }
    Given path '/teacher-notebook/v1/subject-classes/1/exercises'
    And request createRequest
    When method PUT
    Then status 400
    And match response.code == "1006"
    And match response.description == "VALIDATION_ERROR"
    And match response.details contains deep [{ field: 'percentageGrade', reason: '#string' }]

  Scenario: Create exercise - Validation error (missing maxGrade)
    * def createRequest = { title: 'Exam', quarter: 1, percentageGrade: 30 }
    Given path '/teacher-notebook/v1/subject-classes/1/exercises'
    And request createRequest
    When method PUT
    Then status 400
    And match response.code == "1006"
    And match response.description == "VALIDATION_ERROR"
    And match response.details contains deep [{ field: 'maxGrade', reason: '#string' }]

  Scenario: Create exercise - Validation error (invalid maxGrade)
    * def createRequest = { title: 'Exam', quarter: 1, percentageGrade: 30, maxGrade: 16 }
    Given path '/teacher-notebook/v1/subject-classes/1/exercises'
    And request createRequest
    When method PUT
    Then status 400
    And match response.code == "1006"
    And match response.description == "VALIDATION_ERROR"
    And match response.details contains deep [{ field: 'maxGrade', reason: '#string' }]

  Scenario: Create exercise - Subject class not found
    * def createRequest = { title: 'Exam', quarter: 1, percentageGrade: 30, maxGrade: 10 }
    Given path '/teacher-notebook/v1/subject-classes/9999/exercises'
    And request createRequest
    When method PUT
    Then status 404
    And match response.code == "1003"
    And match response.description == "RESOURCE_NOT_FOUND"

