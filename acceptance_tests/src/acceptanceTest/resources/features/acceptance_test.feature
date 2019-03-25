Feature: Acceptance test
    As a developer
    I want to be able to login
    So that I see that the site works

    Background:
        Given the following companies exist
        | company                 | slug      |
        | Acceptance Test Company | ac-comp   |
        | Test Corp               | test-corp |
        And the following users exist
        | name     | email          | password      | company   |
        | at-user  | at@example.com | dummyPassw0rd | Test Corp |

    Scenario Outline: Login to EVO
        Given I am on the login page
        When I login as user '<email>' and password '<password>'
        Then I should see the Dashboard

        Examples:
            | email          | password      | name    |
            | at@example.com | dummyPassw0rd | at-user |

    Scenario Outline: Deny login to EVO
        Given I am on the login page
        When I login as user '<email>' and password '<password>'
        Then I should see error message '<message>'

        Examples:
            | email          | password | message         |
            | at@example.com | nope     | Bad credentials |
