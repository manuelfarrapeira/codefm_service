Feature: Login para obtener cookie

  @ignore
  Scenario: User login
    * def credentials = username + ':' + password
    * def encoded = java.util.Base64.getEncoder().encodeToString(credentials.getBytes('UTF-8'))
    Given url 'https://codefm.synology.me:5553'
    And path '/public/auth/login'
    And header Authorization = 'Basic ' + encoded
    When method POST
    Then status 200
    * def cookie = responseHeaders['Set-Cookie'][1]
    * def result = { authCookie: cookie }
    * karate.set('authCookie', cookie)
