@Regresion
Feature: Teacher Notebook - Create Skill

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl


  Scenario Outline: Fail to create a skill with invalid data
    * def requestBody = { title: <title>, description: <description> }
    * if (requestBody.title == null) karate.remove('requestBody', 'title')
    * if (requestBody.description == null) karate.remove('requestBody', 'description')

    Given path '/teacher-notebook/v1/skills'
    And request requestBody
    When method PUT
    Then status 400
    And match response.code == "1006"
    And match response.description == "VALIDATION_ERROR"
    And match response.details contains deep <details>

    Examples:
      | title               | description          | details                                                                                              |
      | null                | 'Descripcion valida' | [{ field: 'title', reason: 'El título de la competencia es obligatorio.' }]                  |
      | ""                  | 'Descripcion valida' | [{ field: 'title', reason: 'El título de la competencia es obligatorio.' }]                  |
      | "  "                | 'Descripcion valida' | [{ field: 'title', reason: 'El título de la competencia es obligatorio.' }]                  |
      | "Abc"               | 'Descripcion valida' | [{ field: 'title', reason: 'El título de la competencia debe tener al menos 5 caracteres.' }]|
      | 'Titulo valido'     | null                 | [{ field: 'description', reason: 'La descripción de la competencia es obligatoria.' }]       |
      | 'Titulo valido'     | ""                   | [{ field: 'description', reason: 'La descripción de la competencia es obligatoria.' }]       |
      | 'Titulo valido'     | "  "                 | [{ field: 'description', reason: 'La descripción de la competencia es obligatoria.' }]       |
      | 'Titulo valido'     | "Abc"                | [{ field: 'description', reason: 'La descripción de la competencia debe tener al menos 5 caracteres.' }]|


