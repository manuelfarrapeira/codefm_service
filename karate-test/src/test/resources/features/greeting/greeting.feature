@Regresion
Feature: Authenticated greeting

  Background:
    * configure headers = { Cookie: '#(authTokens.karateuseradmin)' }
    Given url baseHttpsUrl

  Scenario Outline: Validate user greeting
    Given path '/greeting'
    And param user = '<user>'
    When method GET
    Then status <status>
    And match response == '<expectedResponse>'

    Examples:
      | user        | expectedResponse            | status
      | mfarrapeira | Hi! Manuel Farrapeira Pérez | 200
      | user        | User not found              | 200