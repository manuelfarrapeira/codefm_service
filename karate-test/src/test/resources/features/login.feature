@Regresion
Feature: Login para obtener cookie

  Background:
    Given url baseHttpsUrl

  Scenario: User not found
    * def credentials = 'user:pass'
    * def encoded = java.util.Base64.getEncoder().encodeToString(credentials.getBytes('UTF-8'))
    And path '/public/auth/login'
    And header Authorization = 'Basic ' + encoded
    When method POST
    Then status 404


  Scenario: Logout
    And path '/public/auth/logout'
    When method POST
    Then status 200
