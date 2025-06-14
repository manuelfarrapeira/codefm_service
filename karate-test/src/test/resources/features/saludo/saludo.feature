@Regresion
Feature: Saludo autenticado

  Background:
    * configure headers = { Cookie: '#(authTokens.karateuseradmin)' }
    Given url baseHttpsUrl

  Scenario Outline: Validar saludo de usuarios
    Given path '/saludo/hola'
    And param usuario = '<usuario>'
    When method GET
    Then status <status>
    And match response == '<respuestaEsperada>'

    Examples:
      | usuario     | respuestaEsperada            | status
      | mfarrapeira | Hola Manuel Farrapeira Pérez | 200
      | usuario     | Usuario no encontrado        | 200