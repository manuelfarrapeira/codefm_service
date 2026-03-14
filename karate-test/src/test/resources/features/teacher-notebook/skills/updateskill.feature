@Regresion
Feature: Teacher Notebook - Update Skill

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

  Scenario: Update a skill successfully
    * def updateBody = { title: 'Competencia en comunicación lingüística (CCL)', description: 'Uso de distintas lenguas para comunicarse, valorando la diversidad cultural y lingüística' }
    Given path '/teacher-notebook/v1/skills/1'
    And request updateBody
    When method PATCH
    Then status 200
    And match response.id == 1
    And match response.title == 'Competencia en comunicación lingüística (CCL)'
    And match response.description == 'Uso de distintas lenguas para comunicarse, valorando la diversidad cultural y lingüística'

  Scenario Outline: Update skill with invalid data
    * def requestBody = { title: <title>, description: <description> }
    * if (requestBody.title == null) karate.remove('requestBody', 'title')
    * if (requestBody.description == null) karate.remove('requestBody', 'description')

    Given path '/teacher-notebook/v1/skills/1'
    And request requestBody
    When method PATCH
    Then status 400
    And match response.code == '1006'
    And match response.description == 'VALIDATION_ERROR'
    And match response.details contains deep <details>

    Examples:
      | title           | description          | details                                                                                              |
      | null            | 'Descripcion valida' | [{ field: 'title', reason: 'El título de la competencia es obligatorio.' }]                  |
      | ""              | 'Descripcion valida' | [{ field: 'title', reason: 'El título de la competencia es obligatorio.' }]                  |
      | 'Titulo valido' | null                 | [{ field: 'description', reason: 'La descripción de la competencia es obligatoria.' }]       |
      | 'Titulo valido' | ""                   | [{ field: 'description', reason: 'La descripción de la competencia es obligatoria.' }]       |

  Scenario: Fail to update skill that does not exist
    * def requestBody = { title: 'Valid Title', description: 'New Description' }

    Given path '/teacher-notebook/v1/skills/999'
    And request requestBody
    When method PATCH
    Then status 404
    And match response.code == '1003'
    And match response.description == 'RESOURCE_NOT_FOUND'
    And match response.detail == 'Competencia no encontrada.'

  Scenario: Forbiden teacher tries to update a skill
    * def requestBody = { title: 'Valid Title', description: 'New Description' }

    Given path '/teacher-notebook/v1/skills/9'
    And request requestBody
    When method PATCH
    Then status 403
    And match response.code == '1004'
    And match response.description == 'RESOURCE_FORBIDDEN'
    And match response.detail == 'No está autorizado para realizar cambios en esta competencia.'
