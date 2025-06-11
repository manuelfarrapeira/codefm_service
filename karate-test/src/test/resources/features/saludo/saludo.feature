@Regresion
Feature: Saludo autenticado

  Background:
    * def username = 'karateuseradmin'
    * def password = karate.get('karateuseradmin')
    * def loginResult = call read('classpath:features/authenticate/auth.feature') { username: '#(username)', password: '#(password)' }
    * def authCookie = loginResult.authCookie
    * configure headers = { Cookie: '#(authCookie)' }
    Given url baseHttpsUrl

  Scenario: Usuario encontrado
    Given path '/saludo/hola'
    And param usuario = 'mfarrapeira'
    When method GET
    Then status 200
    And match response == 'Hola Manuel Farrapeira Pérez'

  Scenario: Usuario no encontrado
    Given path '/saludo/hola'
    And param usuario = 'usuario'
    When method GET
    Then status 200
    And match response == 'Usuario no encontrado'
