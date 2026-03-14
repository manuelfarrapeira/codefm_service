@Regresion
Feature: Teacher Notebook - Delete Skill

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl


  Scenario: Fail to delete skill that does not exist
    Given path '/teacher-notebook/v1/skills/999'
    When method DELETE
    Then status 404
    And match response.code == '1003'
    And match response.description == 'RESOURCE_NOT_FOUND'
    And match response.detail == 'Competencia no encontrada.'


  Scenario: Fail to delete skill that Forbiden skill
    Given path '/teacher-notebook/v1/skills/9'
    When method DELETE
    Then status 403
    And match response.code == '1004'
    And match response.description == 'RESOURCE_FORBIDDEN'
    And match response.detail == 'No está autorizado para realizar cambios en esta competencia.'