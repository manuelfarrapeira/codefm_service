Feature: Obtener secretos desde Infisical

  @ignore
  Scenario: Obtener secretos del entorno
    * def baseUrl = 'http://codefm.synology.me:3080'
    * def token = __arg.token
    * def workspaceId = '491fe430-7bf6-40b0-aa05-58d7f5268b28'

    Given url baseUrl + '/api/v3/secrets/raw'
    And header Authorization = 'Bearer ' + token
    And param workspaceId = workspaceId
    When method GET
    Then status 200

    * def secrets = response.secrets
    * eval karate.forEach(secrets, function(s) { karate.set(s.secretKey, s.secretValue) })