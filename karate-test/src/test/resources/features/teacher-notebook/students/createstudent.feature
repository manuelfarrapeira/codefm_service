@Regresion
Feature: Teacher Notebook - Create Student

  Background:
    * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
    Given url baseHttpsUrl

#  Scenario: Create a new student successfully
#    * def requestBody =
#      """
#      {
#        "name": "Juan Carlos",
#        "surnames": "García López",
#        "dateOfBirth": "15/03/2010",
#        "gender": "M",
#        "additionalInfo": "Estudiante ejemplar"
#      }
#      """
#    Given path '/teacher-notebook/v1/students'
#    And request requestBody
#    When method PUT
#    Then status 201
#    And match response.id == '#number'
#    And match response.name == "Juan Carlos"
#    And match response.surnames == "García López"
#    And match response.dateOfBirth == "15/03/2010"
#    And match response.gender == "M"
#    And match response.additionalInfo == "Estudiante ejemplar"

  Scenario Outline: Fail to create a student with invalid data
    * def requestBody = { name: <name>, surnames: <surnames>, dateOfBirth: <dateOfBirth>, gender: <gender> }
    * if (requestBody.name == null) karate.remove('requestBody', 'name')
    * if (requestBody.surnames == null) karate.remove('requestBody', 'surnames')
    * if (requestBody.dateOfBirth == null) karate.remove('requestBody', 'dateOfBirth')
    * if (requestBody.gender == null) karate.remove('requestBody', 'gender')

    Given path '/teacher-notebook/v1/students'
    And request requestBody
    When method PUT
    Then status 400
    And match response.code == "1006"
    And match response.description == "VALIDATION_ERROR"
    And match response.details contains deep <details>

    Examples:
      | name          | surnames        | dateOfBirth  | gender | details                                                                                                                                                   |
      | null          | "García López"  | "15/03/2010" | "M"    | [{ field: 'name', reason: 'El nombre del estudiante es obligatorio.' }]                                                                                   |
      | "A"           | "García López"  | "15/03/2010" | "M"    | [{ field: 'name', reason: 'El nombre del estudiante debe tener al menos 3 caracteres.' }]                                                                 |
      | "Juan"        | null            | "15/03/2010" | "M"    | [{ field: 'surnames', reason: 'Los apellidos del estudiante son obligatorios.' }]                                                                         |
      | "Juan"        | "AB"            | "15/03/2010" | "M"    | [{ field: 'surnames', reason: 'Los apellidos del estudiante deben tener al menos 3 caracteres.' }]                                                        |
      | "Juan"        | "García López"  | "15-03-2010" | "M"    | [{ field: 'dateOfBirth', reason: 'La fecha de nacimiento debe estar en formato dd/MM/yyyy.' }]                                                            |
      | "Juan"        | "García López"  | "15/03/2010" | null   | [{ field: 'gender', reason: 'El género del estudiante es obligatorio.' }]                                                                                 |
      | "Juan"        | "García López"  | "15/03/2010" | "X"    | [{ field: 'gender', reason: 'El género debe ser M (masculino) o F (femenino).' }]                                                                         |
      | null          | null            | "15/03/2010" | "M"    | [{ field: 'name', reason: 'El nombre del estudiante es obligatorio.' }, { field: 'surnames', reason: 'Los apellidos del estudiante son obligatorios.' }] |

