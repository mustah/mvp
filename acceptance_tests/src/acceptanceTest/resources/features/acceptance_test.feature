Feature: Acceptance test
    As a developer
    I want to be able to login
    So that I see that the site works

    Scenario Outline: Login to MVP
        Given I am on the login page
        When I login as user '<user>' and password '<password>'
        Then I should be logged in as '<displayname>'

        Examples:
            | user                 | password | displayname          |
            | vaxjo@vaxjoenergi.se | vaxjo    | vaxjo@vaxjoenergi.se |

    Scenario Outline: Deny login to MVP
        Given I am on the login page
        When I login as user '<user>' and password '<password>'
        Then I should see error message '<message>'

        Examples:
            | user                    | password | message                               |
            | blackbox-test@elvaco.se | nope     | Bad credentials |
