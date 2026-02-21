@Regresion
Feature: Update Exercise

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Update exercise - Success
    * def updateRequest = { title: 'Updated Exam Title', description: 'Updated description', quarter: 2, percentageGrade: 50, maxGrade: 12 }
    Given path '/teacher-notebook/v1/exercises/1'
    And request updateRequest
    When method PATCH
    Then status 200
    And match response.title == 'Updated Exam Title'
    And match response.percentageGrade == 50
    And match response.maxGrade == 12

  Scenario: Update exercise - Not found
    * def updateRequest = { title: 'Updated', quarter: 1, percentageGrade: 30, maxGrade: 10 }
    Given path '/teacher-notebook/v1/exercises/9999'
    And request updateRequest
    When method PATCH
    Then status 404
    And match response.code == "1003"
    And match response.description == "RESOURCE_NOT_FOUND"

  Scenario: Update exercise - Validation error (empty title)
    * def updateRequest = { title: '', quarter: 1, percentageGrade: 30, maxGrade: 10 }
    Given path '/teacher-notebook/v1/exercises/1'
    And request updateRequest
    When method PATCH
    Then status 400
    And match response.code == "1006"
    And match response.description == "VALIDATION_ERROR"
    And match response.details contains deep [{ field: 'title', reason: '#string' }]

  Scenario: Update exercise - Validation error (invalid quarter)
    * def updateRequest = { title: 'Exam', quarter: 0, percentageGrade: 30, maxGrade: 10 }
    Given path '/teacher-notebook/v1/exercises/1'
    And request updateRequest
    When method PATCH
    Then status 400
    And match response.code == "1006"
    And match response.description == "VALIDATION_ERROR"
    And match response.details contains deep [{ field: 'quarter', reason: '#string' }]

  Scenario: Update exercise - Validation error (invalid percentageGrade)
    * def updateRequest = { title: 'Exam', quarter: 1, percentageGrade: 0, maxGrade: 10 }
    Given path '/teacher-notebook/v1/exercises/1'
    And request updateRequest
    When method PATCH
    Then status 400
    And match response.code == "1006"
    And match response.description == "VALIDATION_ERROR"
    And match response.details contains deep [{ field: 'percentageGrade', reason: '#string' }]

  Scenario: Update exercise - Validation error (invalid maxGrade)
    * def updateRequest = { title: 'Exam', quarter: 1, percentageGrade: 30, maxGrade: 16 }
    Given path '/teacher-notebook/v1/exercises/1'
    And request updateRequest
    When method PATCH
    Then status 400
    And match response.code == "1006"
    And match response.description == "VALIDATION_ERROR"
    And match response.details contains deep [{ field: 'maxGrade', reason: '#string' }]

