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
        | username | email          | password      | company   |
        | at-user  | at@example.com | dummyPassw0rd | Test Corp |

    Scenario Outline: Login to MVP
        Given I am on the login page
        When I login as user '<user>' and password '<password>'
        Then I should be logged in as '<displayname>'

        Examples:
            | user           | password      | displayname |
            | at@example.com | dummyPassw0rd | at-user     |

    Scenario Outline: Deny login to MVP
        Given I am on the login page
        When I login as user '<user>' and password '<password>'
        Then I should see error message '<message>'

        Examples:
            | user           | password | message         |
            | at@example.com | nope     | Bad credentials |
