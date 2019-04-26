Feature: Errors

  Scenario: Non-existent endpoint should return 401 when not authenticated
    Given url api_endpoint + '/api/v1/not_an_end_point'
    When method get
    Then status 401
