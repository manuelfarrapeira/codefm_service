@Regresion
Feature: Delete School API

  Background:
      * configure headers = { 'Cookie': '#(authTokens.karateuseradmin)', 'Accept-Language': 'es' }
      Given url baseLocalUrl
    * url baseLocalUrl


    Scenario Outline: Fail to delete school
        Given path '/teacher-notebook/schools/' + <schoolId>
        When method DELETE
        Then status <status>
        And match response.code == <code>


        Examples:
          | schoolId | status      | code
          | 999      | 404         | '1003'
          | 13       | 403         | '1004'

