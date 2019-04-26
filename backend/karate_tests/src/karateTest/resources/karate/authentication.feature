Feature: Authentication & login

  Scenario: Login with valid credentials succeeds
    Given url api_endpoint + '/api/v1/authenticate'
    * header Authorization = call read('basic-auth.js') {username: 'mvpadmin@elvaco.se', password: 'changeme'}
    When method get
    Then status 200

  Scenario: Login with invalid credentials fails
    Given url api_endpoint + '/api/v1/authenticate'
    * header Authorization = call read('basic-auth.js') {username: 'mvpadmin@elvaco.se', password: 'not-this-users-password'}
    When method get
    Then status 401
