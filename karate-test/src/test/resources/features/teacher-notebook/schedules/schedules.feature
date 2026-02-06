Feature: Schedule Endpoints

  Background:
    Given url baseHttpsUrl
    And cookie SESSION = authTokens.karateuseradmin

  Scenario: Get schedules by class - Success
    * def scheduleSchema = { id: '#number', classId: '#number', subjectId: '#number', day: '#number', start: '#string', end: '#string' }
    Given path '/teacher-notebook/v1/classes/1/schedules'
    When method GET
    Then status 200
    And match each response == scheduleSchema

  Scenario: Get schedules by class - Class not found
    Given path '/teacher-notebook/v1/classes/9999/schedules'
    When method GET
    Then status 404

  Scenario: Create schedules for a class - Success
    * def createRequest = { day: 1, items: [{ subjectId: 1, start: '08:30', end: '09:30' }, { subjectId: 1, start: '09:30', end: '10:30' }] }
    Given path '/teacher-notebook/v1/classes/1/schedules'
    And request createRequest
    When method PUT
    Then status 201
    And match each response contains { classId: 1, day: 1 }

  Scenario: Create schedules - Validation error (invalid day)
    * def createRequest = { day: 6, items: [{ subjectId: 1, start: '08:30', end: '09:30' }] }
    Given path '/teacher-notebook/v1/classes/1/schedules'
    And request createRequest
    When method PUT
    Then status 400

  Scenario: Create schedules - Validation error (end before start)
    * def createRequest = { day: 1, items: [{ subjectId: 1, start: '10:30', end: '09:30' }] }
    Given path '/teacher-notebook/v1/classes/1/schedules'
    And request createRequest
    When method PUT
    Then status 400

  Scenario: Create schedules - Subject not found
    * def createRequest = { day: 1, items: [{ subjectId: 9999, start: '08:30', end: '09:30' }] }
    Given path '/teacher-notebook/v1/classes/1/schedules'
    And request createRequest
    When method PUT
    Then status 400

  Scenario: Update a schedule - Success
    * def updateRequest = { day: 2, start: '09:00', end: '10:00' }
    Given path '/teacher-notebook/v1/schedules/1'
    And request updateRequest
    When method PATCH
    Then status 200
    And match response.day == 2
    And match response.start == '09:00'
    And match response.end == '10:00'

  Scenario: Update a schedule - Not found
    * def updateRequest = { day: 2, start: '09:00', end: '10:00' }
    Given path '/teacher-notebook/v1/schedules/9999'
    And request updateRequest
    When method PATCH
    Then status 404

  Scenario: Update a schedule - Validation error (invalid day)
    * def updateRequest = { day: 7, start: '09:00', end: '10:00' }
    Given path '/teacher-notebook/v1/schedules/1'
    And request updateRequest
    When method PATCH
    Then status 400

  Scenario: Delete schedules - Success
    * def deleteRequest = { ids: [1, 2] }
    Given path '/teacher-notebook/v1/schedules'
    And request deleteRequest
    When method DELETE
    Then status 204

  Scenario: Delete schedules - Validation error (empty ids)
    * def deleteRequest = { ids: [] }
    Given path '/teacher-notebook/v1/schedules'
    And request deleteRequest
    When method DELETE
    Then status 400

  Scenario: Delete schedules - Validation error (ids not owned)
    * def deleteRequest = { ids: [9999] }
    Given path '/teacher-notebook/v1/schedules'
    And request deleteRequest
    When method DELETE
    Then status 400
